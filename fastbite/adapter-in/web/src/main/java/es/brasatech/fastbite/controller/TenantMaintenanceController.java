package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.tenant.TenantBackupRestorePort;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.util.Map;

@Controller
@RequestMapping("/{tenantId}/api/backoffice/maintenance")
@RequiredArgsConstructor
@Slf4j
public class TenantMaintenanceController {

    private final TenantBackupRestorePort tenantBackupRestorePort;

    @GetMapping("/backup")
    public void downloadBackup(@PathVariable String tenantId, HttpServletResponse response) {
        log.info("Requested database and media backup for tenant: {}", tenantId);
        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + tenantId + "_backup.zip");

        try (BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            tenantBackupRestorePort.exportBackup(tenantId, bos);
            bos.flush();
            log.info("Backup successfully streamed for tenant: {}", tenantId);
        } catch (Exception e) {
            log.error("Failed to stream backup for tenant: " + tenantId, e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @PostMapping("/restore")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadRestore(
            @PathVariable String tenantId,
            @RequestParam("file") MultipartFile file) {
        log.info("Requested database and media restore for tenant: {}", tenantId);
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Uploaded backup file is empty."));
        }

        try (InputStream is = file.getInputStream()) {
            tenantBackupRestorePort.importRestore(tenantId, is);
            log.info("Restore completed successfully for tenant: {}", tenantId);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Database and files successfully restored."));
        } catch (Exception e) {
            log.error("Failed to restore backup for tenant: " + tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to restore: " + e.getMessage()));
        }
    }

    @PostMapping("/clean")
    @ResponseBody
    public ResponseEntity<Map<String, String>> cleanData(@PathVariable String tenantId) {
        log.info("Requested database reset/clean for tenant: {}", tenantId);
        try {
            tenantBackupRestorePort.cleanTenantData(tenantId);
            log.info("Tenant reset/clean completed successfully: {}", tenantId);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Database and uploads successfully reset."));
        } catch (Exception e) {
            log.error("Failed to reset tenant data for: " + tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to reset data: " + e.getMessage()));
        }
    }

    @PostMapping("/restore-demo")
    @ResponseBody
    public ResponseEntity<Map<String, String>> restoreDemo(@PathVariable String tenantId) {
        log.info("Requested restore from built-in demo package for tenant: {}", tenantId);
        try (InputStream is = getClass().getResourceAsStream("/kebab_demo.zip")) {
            if (is == null) {
                log.warn("Built-in kebab_demo.zip not found in classpath. Attempting project root fallback.");
                java.nio.file.Path rootZip = java.nio.file.Paths.get("d:/git/fastbite/kebab_demo.zip");
                if (java.nio.file.Files.exists(rootZip)) {
                    try (InputStream fis = java.nio.file.Files.newInputStream(rootZip)) {
                        tenantBackupRestorePort.importRestore(tenantId, fis);
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("message", "Demo backup package not found on server."));
                }
            } else {
                tenantBackupRestorePort.importRestore(tenantId, is);
            }
            log.info("Demo restore completed successfully for tenant: {}", tenantId);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Restaurant template demo data successfully loaded."));
        } catch (Exception e) {
            log.error("Failed to restore demo for tenant: " + tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to load demo: " + e.getMessage()));
        }
    }
}
