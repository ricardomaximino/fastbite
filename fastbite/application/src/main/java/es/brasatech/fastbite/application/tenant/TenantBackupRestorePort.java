package es.brasatech.fastbite.application.tenant;

import java.io.InputStream;
import java.io.OutputStream;

public interface TenantBackupRestorePort {
    void exportBackup(String tenantId, OutputStream outputStream);
    void importRestore(String tenantId, InputStream inputStream);
    void cleanTenantData(String tenantId);
}
