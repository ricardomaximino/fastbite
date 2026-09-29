package es.brasatech.fastbite.jpa.tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantSchemaInitializer implements InitializingBean {

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initializing tenant schemas...");

        // The platform schema (which also holds the kebab demo) plus every tenant schema, so that
        // schema changes and migrations reach all of them; Hibernate only updates the platform schema.
        Set<String> tenants = new LinkedHashSet<>();
        tenants.add("default");
        try (Connection connection = dataSource.getConnection();
             ResultSet rs = connection.getMetaData().getSchemas()) {
            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                if (schemaName != null && schemaName.toLowerCase().startsWith("tenant_") && !schemaName.equalsIgnoreCase("tenant_kebab")) {
                    tenants.add(schemaName.substring("tenant_".length()).toLowerCase());
                }
            }
        } catch (Exception e) {
            log.error("Failed to discover tenant schemas from database metadata", e);
        }

        // Clean up / drop the old kebab tenant schema completely if it exists
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP SCHEMA IF EXISTS tenant_kebab CASCADE");
                log.info("Successfully dropped legacy tenant_kebab schema");
            }
        } catch (Exception e) {
            log.warn("Failed to drop old tenant_kebab schema: {}", e.getMessage());
        }

        for (String tenant : tenants) {
            initializeSchema(tenant);
        }
        log.info("Tenant schemas initialized successfully.");
    }

    public void initializeSchema(String tenantId) {
        try (Connection connection = dataSource.getConnection()) {
            String schemaName = TenantSchemaUtils.resolveSchemaName(connection, tenantId);
            log.info("Initializing schema for tenant: {}", schemaName);

            TenantSchemaUtils.createAndSwitchSchema(connection, schemaName);

            Resource schemaResource = resourceLoader.getResource("classpath:schema.sql");
            if (schemaResource.exists()) {
                ScriptUtils.executeSqlScript(connection, schemaResource);
                log.info("Successfully executed schema.sql for tenant: {}", schemaName);
            } else {
                log.warn("schema.sql not found in classpath!");
            }
            LegacyTranslationMigration.migrate(connection, schemaName);
            // Demo content is loaded from kebab_demo.zip (DemoDataInitializer / "Restore demo"), not seeded here.
        } catch (Exception e) {
            log.error("Failed to initialize schema for tenant: " + tenantId, e);
        }
    }
}
