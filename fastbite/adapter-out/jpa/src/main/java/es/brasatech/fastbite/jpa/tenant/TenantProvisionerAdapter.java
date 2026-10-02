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

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantProvisionerAdapter implements TenantProvisionerPort {

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    @Override
    public void provisionTenant(String tenantId) {
        String schemaName = "tenant_" + es.brasatech.fastbite.application.tenant.TenantRegistrationRules.tenantId(tenantId);
        log.info("Provisioning database schema for tenant: {}", schemaName);
        try (Connection connection = dataSource.getConnection()) {
            try {
                TenantSchemaUtils.createAndSwitchSchema(connection, schemaName);
                Resource schemaResource = resourceLoader.getResource("classpath:schema.sql");
                if (!schemaResource.exists()) throw new IllegalStateException("Registration schema resource is missing");
                ScriptUtils.executeSqlScript(connection, schemaResource);
                log.info("Successfully executed schema.sql for tenant: {}", schemaName);
            } finally {
                TenantSchemaUtils.switchSchema(connection, TenantSchemaUtils.resolveDefaultSchemaName(connection));
            }
        } catch (Exception e) {
            log.error("Failed to provision schema for tenant: " + schemaName, e);
            throw new RuntimeException("Failed to provision tenant database", e);
        }
    }
}
