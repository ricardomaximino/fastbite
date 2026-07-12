package es.brasatech.fastbite.jpa.tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantSchemaInitializer implements org.springframework.beans.factory.InitializingBean {

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initializing tenant schemas...");
        List<String> tenants = List.of("default", "kebab");
        for (String tenant : tenants) {
            initializeSchema(tenant);
        }
        log.info("Tenant schemas initialized successfully.");
    }

    public void initializeSchema(String tenantId) {
        String schemaName = "tenant_" + tenantId;
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

            if ("tenant_kebab".equals(schemaName)) {
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
            }
        } catch (Exception e) {
            log.error("Failed to initialize schema for tenant: " + schemaName, e);
        }
    }
}
