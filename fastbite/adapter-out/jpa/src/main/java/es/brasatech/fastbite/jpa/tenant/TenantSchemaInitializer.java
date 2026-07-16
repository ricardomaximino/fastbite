package es.brasatech.fastbite.jpa.tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantSchemaInitializer implements org.springframework.beans.factory.InitializingBean {

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;
    private final org.springframework.core.env.Environment environment;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initializing tenant schemas...");
        boolean isProd = java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");
        
        java.util.Set<String> tenants = new java.util.HashSet<>();
        
        if (isProd) {
            tenants.add("default");
            try (Connection connection = dataSource.getConnection()) {
                try (java.sql.ResultSet rs = connection.getMetaData().getSchemas()) {
                    while (rs.next()) {
                        String schemaName = rs.getString("TABLE_SCHEM");
                        if (schemaName != null && schemaName.toLowerCase().startsWith("tenant_") && !schemaName.equalsIgnoreCase("tenant_kebab")) {
                            String tenantId = schemaName.substring("tenant_".length()).toLowerCase();
                            tenants.add(tenantId);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to discover tenant schemas from database metadata", e);
            }
        } else {
            tenants.add("kebab");
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
        String schemaName = "default".equalsIgnoreCase(tenantId) || "kebab".equalsIgnoreCase(tenantId) ? "PUBLIC" : "tenant_" + tenantId;
        log.info("Initializing schema for tenant: {}", schemaName);
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
                statement.execute("SET SCHEMA " + schemaName);
            }

            Resource schemaResource = resourceLoader.getResource("classpath:schema.sql");
            if (schemaResource.exists()) {
                ScriptUtils.executeSqlScript(connection, schemaResource);
                log.info("Successfully executed schema.sql for tenant: {}", schemaName);
            } else {
                log.warn("schema.sql not found in classpath!");
            }

            boolean hasData = false;
            try (Statement stmt = connection.createStatement()) {
                try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM groups")) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        hasData = true;
                    }
                }
            } catch (Exception e) {
                // Ignore, let script run
            }

            if (!hasData) {
                Resource dataResource = resourceLoader.getResource("classpath:data-jpa.sql");
                if (dataResource.exists()) {
                    ScriptUtils.executeSqlScript(connection, dataResource);
                    log.info("Successfully executed data-jpa.sql for tenant: {}", schemaName);
                } else {
                    log.warn("data-jpa.sql not found in classpath!");
                }
            } else {
                log.info("Initial data already exists for tenant: {}, skipping data-jpa.sql", schemaName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize schema for tenant: " + schemaName, e);
        }
    }
}
