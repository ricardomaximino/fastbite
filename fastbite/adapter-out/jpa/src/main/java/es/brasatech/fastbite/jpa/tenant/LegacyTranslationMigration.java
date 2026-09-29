package es.brasatech.fastbite.jpa.tenant;

import es.brasatech.fastbite.jpa.i18n.TranslationsConverter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One-time move of translations from the legacy {@code *_translations} tables into each row's
 * {@code translations} column. Runs per schema at startup; a legacy table is dropped once its rows are copied,
 * so later runs find nothing to do.
 */
@Slf4j
final class LegacyTranslationMigration {

    private record LegacyTable(String name, String parentTable, String parentColumn, List<String> fields) {
    }

    private static final List<LegacyTable> TABLES = List.of(
            new LegacyTable("group_translations", "groups", "group_id", List.of("name", "description")),
            new LegacyTable("product_translations", "products", "product_id", List.of("name", "description")),
            new LegacyTable("customization_translations", "customizations", "customization_id", List.of("name")),
            new LegacyTable("customization_option_translations", "customization_options", "customization_option_id", List.of("name")),
            new LegacyTable("discount_rule_translations", "discount_rules", "discount_rule_id", List.of("name")),
            new LegacyTable("table_translations", "dining_tables", "table_id", List.of("name")));

    private static final TranslationsConverter CONVERTER = new TranslationsConverter();

    private LegacyTranslationMigration() {
    }

    static void migrate(Connection connection, String schemaName) throws SQLException {
        for (LegacyTable table : TABLES) {
            if (tableExists(connection, schemaName, table.name())) {
                migrate(connection, schemaName, table);
            }
        }
    }

    private static void migrate(Connection connection, String schemaName, LegacyTable table) throws SQLException {
        // Always schema-qualified: on PostgreSQL the search path also includes public.
        String legacyTable = schemaName + "." + table.name();
        Map<String, Map<String, Map<String, String>>> byParent = new HashMap<>();

        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT " + table.parentColumn() + ", language, "
                     + String.join(", ", table.fields()) + " FROM " + legacyTable)) {
            while (rows.next()) {
                Map<String, String> fields = byParent.computeIfAbsent(rows.getString(1), id -> new HashMap<>())
                        .computeIfAbsent(rows.getString(2), language -> new HashMap<>());
                for (String field : table.fields()) {
                    String value = rows.getString(field);
                    if (value != null && !value.isBlank()) {
                        fields.put(field, value);
                    }
                }
            }
        }

        try (PreparedStatement update = connection.prepareStatement("UPDATE " + schemaName + "." + table.parentTable()
                + " SET translations = ? WHERE id = ? AND translations IS NULL")) {
            for (var entry : byParent.entrySet()) {
                update.setString(1, CONVERTER.convertToDatabaseColumn(entry.getValue()));
                update.setString(2, entry.getKey());
                update.addBatch();
            }
            update.executeBatch();
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE " + legacyTable);
        }
        log.info("Moved translations of {} rows from {} into {}.{}.translations",
                byParent.size(), legacyTable, schemaName, table.parentTable());
    }

    private static boolean tableExists(Connection connection, String schemaName, String tableName) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE LOWER(table_schema) = LOWER(?) AND LOWER(table_name) = ?")) {
            query.setString(1, schemaName);
            query.setString(2, tableName);
            try (ResultSet result = query.executeQuery()) {
                return result.next() && result.getInt(1) > 0;
            }
        }
    }
}
