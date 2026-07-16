package es.brasatech.fastbite.config;

import es.brasatech.fastbite.application.tenant.TenantBackupRestorePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class DemoDataInitializer implements CommandLineRunner {

    private final TenantBackupRestorePort tenantBackupRestorePort;
    private final String uploadDirectory;

    public DemoDataInitializer(TenantBackupRestorePort tenantBackupRestorePort,
                               @org.springframework.beans.factory.annotation.Value("${image.upload.directory}") String uploadDirectory) {
        this.tenantBackupRestorePort = tenantBackupRestorePort;
        this.uploadDirectory = uploadDirectory;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking demo data initialization for default tenants...");
        
        initializeTenantDemo("default");
        initializeTenantDemo("kebab");
    }

    private void initializeTenantDemo(String tenantId) {
        try {
            // Check if tenant upload directory is empty/missing
            Path tenantComboPath = Paths.get(uploadDirectory).resolve(tenantId).resolve("combo");
            if (!Files.exists(tenantComboPath)) {
                log.info("Provisioning demo data template for tenant: {}", tenantId);
                try (InputStream is = getClass().getResourceAsStream("/kebab_demo.zip")) {
                    if (is != null) {
                        tenantBackupRestorePort.importRestore(tenantId, is);
                        log.info("Successfully loaded default demo template for tenant: {}", tenantId);
                    } else {
                        log.warn("kebab_demo.zip template not found in classpath during startup initialization.");
                    }
                }
            } else {
                log.info("Demo data already loaded for tenant: {}", tenantId);
            }
        } catch (Exception e) {
            log.error("Failed to auto-provision demo data for tenant: " + tenantId, e);
        }
    }
}
