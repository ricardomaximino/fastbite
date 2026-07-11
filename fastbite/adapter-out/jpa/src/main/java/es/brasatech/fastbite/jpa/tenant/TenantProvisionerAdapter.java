package es.brasatech.fastbite.jpa.tenant;

import es.brasatech.fastbite.application.tenant.TenantProvisionerPort;
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
public class TenantProvisionerAdapter implements TenantProvisionerPort {

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    @Override
    public void provisionTenant(String tenantId) {
        String schemaName = "tenant_" + tenantId;
        log.info("Provisioning database schema for tenant: {}", schemaName);
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
                log.warn("schema.sql not found in classpath for provisioning!");
            }
        } catch (Exception e) {
            log.error("Failed to provision schema for tenant: " + schemaName, e);
            throw new RuntimeException("Failed to provision tenant database", e);
        }
    }
}
