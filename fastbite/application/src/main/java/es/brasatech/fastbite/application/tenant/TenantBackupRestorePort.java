package es.brasatech.fastbite.application.tenant;

import java.io.InputStream;
import java.io.OutputStream;

public interface TenantBackupRestorePort {
    void exportBackup(String tenantId, OutputStream outputStream);
    void importRestore(String tenantId, InputStream inputStream);
    void importDemoTemplate(String tenantId, InputStream inputStream);
    void cleanTenantData(String tenantId);
    BackupPreview previewBackup(String tenantId, InputStream inputStream);
    record BackupPreview(int products, int orders, int accounts) {}
}
