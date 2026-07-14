package es.brasatech.fastbite.config;

import es.brasatech.fastbite.application.tenant.TenantBackupRestorePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataExporter implements CommandLineRunner {

    private final TenantBackupRestorePort tenantBackupRestorePort;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking demo data package existence...");
        
        // Target paths to ensure the demo zip is saved for compilation/packaging
        Path rootPath = Paths.get("d:/git/fastbite/kebab_demo.zip");
        Path webAdapterResourcesPath = Paths.get("d:/git/fastbite/fastbite/adapter-in/web/src/main/resources/kebab_demo.zip");
        Path webAppResourcesPath = Paths.get("d:/git/fastbite/webapplication/src/main/resources/kebab_demo.zip");

        // We export a fresh demo zip on startup if it's missing from the root or adapter resources
        if (!Files.exists(rootPath) || !Files.exists(webAdapterResourcesPath)) {
            log.info("Generating demo data package (kebab_demo.zip) from seed database...");
            
            // Generate in root first
            try (OutputStream os = new FileOutputStream(rootPath.toFile())) {
                tenantBackupRestorePort.exportBackup("kebab", os);
            }
            log.info("kebab_demo.zip successfully created in project root.");

            // Copy to adapter-in resources
            if (Files.exists(webAdapterResourcesPath.getParent())) {
                Files.copy(rootPath, webAdapterResourcesPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log.info("kebab_demo.zip successfully copied to web adapter resources.");
            }

            // Copy to webapplication resources
            if (Files.exists(webAppResourcesPath.getParent())) {
                Files.copy(rootPath, webAppResourcesPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log.info("kebab_demo.zip successfully copied to webapplication resources.");
            }
        } else {
            log.info("Demo data package (kebab_demo.zip) already exists.");
        }
    }
}
