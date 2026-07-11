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
public class TenantSchemaInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing tenant schemas...");
        List<String> tenants = List.of("default", "1", "2");
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
        } catch (Exception e) {
            log.error("Failed to initialize schema for tenant: " + schemaName, e);
        }
    }
}
