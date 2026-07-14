package es.brasatech.fastbite.jpa.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.brasatech.fastbite.application.tenant.TenantBackupRestorePort;
import es.brasatech.fastbite.domain.tenant.TenantContext;
import es.brasatech.fastbite.jpa.customization.*;
import es.brasatech.fastbite.jpa.discount.DiscountRuleEntity;
import es.brasatech.fastbite.jpa.discount.DiscountRuleJpaRepository;
import es.brasatech.fastbite.jpa.discount.DiscountRuleTranslationEntity;
import es.brasatech.fastbite.jpa.discount.DiscountRuleTranslationJpaRepository;
import es.brasatech.fastbite.jpa.group.GroupEntity;
import es.brasatech.fastbite.jpa.group.GroupJpaRepository;
import es.brasatech.fastbite.jpa.group.GroupTranslationEntity;
import es.brasatech.fastbite.jpa.group.GroupTranslationJpaRepository;
import es.brasatech.fastbite.jpa.order.OrderEntity;
import es.brasatech.fastbite.jpa.order.OrderJpaRepository;
import es.brasatech.fastbite.jpa.payment.PaymentConfigEntity;
import es.brasatech.fastbite.jpa.payment.PaymentConfigJpaRepository;
import es.brasatech.fastbite.jpa.product.ProductEntity;
import es.brasatech.fastbite.jpa.product.ProductJpaRepository;
import es.brasatech.fastbite.jpa.product.ProductTranslationEntity;
import es.brasatech.fastbite.jpa.product.ProductTranslationJpaRepository;
import es.brasatech.fastbite.jpa.table.TableEntity;
import es.brasatech.fastbite.jpa.table.TableJpaRepository;
import es.brasatech.fastbite.jpa.table.TableTranslationEntity;
import es.brasatech.fastbite.jpa.table.TableTranslationJpaRepository;
import es.brasatech.fastbite.jpa.user.UserEntity;
import es.brasatech.fastbite.jpa.user.UserJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantBackupRestoreAdapter implements TenantBackupRestorePort {

    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${image.upload.directory}")
    private String uploadDirectory;

    private final GroupJpaRepository groupJpaRepository;
    private final ProductJpaRepository productJpaRepository;
    private final CustomizationJpaRepository customizationJpaRepository;
    private final CustomizationOptionJpaRepository customizationOptionJpaRepository;
    private final TableJpaRepository tableJpaRepository;
    private final DiscountRuleJpaRepository discountRuleJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final PaymentConfigJpaRepository paymentConfigJpaRepository;

    private final GroupTranslationJpaRepository groupTranslationJpaRepository;
    private final ProductTranslationJpaRepository productTranslationJpaRepository;
    private final CustomizationTranslationJpaRepository customizationTranslationJpaRepository;
    private final CustomizationOptionTranslationJpaRepository customizationOptionTranslationJpaRepository;
    private final DiscountRuleTranslationJpaRepository discountRuleTranslationJpaRepository;
    private final TableTranslationJpaRepository tableTranslationJpaRepository;

    @Override
    @Transactional(readOnly = true)
    public void exportBackup(String tenantId, OutputStream outputStream) {
        String originalTenant = TenantContext.getCurrentTenant();
        try {
            TenantContext.setCurrentTenant(tenantId);
            
            // 1. Gather all database entities
            TenantBackupData backupData = new TenantBackupData();
            backupData.setGroups(groupJpaRepository.findAll());
            backupData.setProducts(productJpaRepository.findAll());
            backupData.setCustomizations(customizationJpaRepository.findAll());
            backupData.setCustomizationOptions(customizationOptionJpaRepository.findAll());
            backupData.setTables(tableJpaRepository.findAll());
            backupData.setDiscountRules(discountRuleJpaRepository.findAll());
            backupData.setOrders(orderJpaRepository.findAll());
            backupData.setUsers(userJpaRepository.findAll());
            backupData.setPaymentConfigs(paymentConfigJpaRepository.findAll());

            backupData.setGroupTranslations(groupTranslationJpaRepository.findAll());
            backupData.setProductTranslations(productTranslationJpaRepository.findAll());
            backupData.setCustomizationTranslations(customizationTranslationJpaRepository.findAll());
            backupData.setCustomizationOptionTranslations(customizationOptionTranslationJpaRepository.findAll());
            backupData.setDiscountRuleTranslations(discountRuleTranslationJpaRepository.findAll());
            backupData.setTableTranslations(tableTranslationJpaRepository.findAll());

            // 2. Write to ZIP
            try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                // Entry 1: Data JSON
                ZipEntry dataEntry = new ZipEntry("data.json");
                zos.putNextEntry(dataEntry);
                objectMapper.writerWithDefaultPrettyPrinter()
                    .without(com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET)
                    .writeValue(zos, backupData);
                zos.closeEntry();

                // Entry 2: Media subfolder
                Path tenantMediaPath = Paths.get(uploadDirectory).resolve(tenantId);
                if (Files.exists(tenantMediaPath) && Files.isDirectory(tenantMediaPath)) {
                    Files.walk(tenantMediaPath)
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            try {
                                String relativePath = "media/" + tenantMediaPath.relativize(file).toString().replace("\\", "/");
                                ZipEntry mediaEntry = new ZipEntry(relativePath);
                                zos.putNextEntry(mediaEntry);
                                Files.copy(file, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                log.error("Failed to copy file to backup zip: " + file, e);
                            }
                        });
                }
            }
        } catch (Exception e) {
            log.error("Failed to export backup for tenant: " + tenantId, e);
            throw new RuntimeException("Export backup failed", e);
        } finally {
            if (originalTenant != null) {
                TenantContext.setCurrentTenant(originalTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    @Override
    @Transactional
    public void importRestore(String tenantId, InputStream inputStream) {
        String originalTenant = TenantContext.getCurrentTenant();
        try {
            TenantContext.setCurrentTenant(tenantId);

            // 1. Clean existing tenant data first
            cleanTenantData(tenantId);
            // Re-set context in case cleanup cleared it
            TenantContext.setCurrentTenant(tenantId);

            TenantBackupData backupData = null;
            Path tenantMediaPath = Paths.get(uploadDirectory).resolve(tenantId);

            // 2. Extract ZIP
            try (ZipInputStream zis = new ZipInputStream(inputStream)) {
                ZipEntry entry;
                byte[] buffer = new byte[4096];
                while ((entry = zis.getNextEntry()) != null) {
                    if ("data.json".equals(entry.getName())) {
                        // Jackson reads data.json
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }
                        backupData = objectMapper.readValue(baos.toByteArray(), TenantBackupData.class);
                    } else if (entry.getName().startsWith("media/") && !entry.isDirectory()) {
                        String relativeFileName = entry.getName().substring("media/".length());
                        Path targetFile = tenantMediaPath.resolve(relativeFileName);
                        Files.createDirectories(targetFile.getParent());
                        try (OutputStream fos = Files.newOutputStream(targetFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    zis.closeEntry();
                }
            }

            if (backupData == null) {
                throw new IllegalArgumentException("Invalid backup file: data.json not found.");
            }

            // 3. Resolve parent references for child/translation entities to prevent TransientPropertyValueException
            org.hibernate.Session session = entityManager.unwrap(org.hibernate.Session.class);

            if (backupData.getCustomizationOptions() != null) {
                for (var opt : backupData.getCustomizationOptions()) {
                    if (opt.getCustomization() != null && opt.getCustomization().getId() != null) {
                        opt.setCustomization(session.getReference(es.brasatech.fastbite.jpa.customization.CustomizationEntity.class, opt.getCustomization().getId()));
                    }
                }
            }

            if (backupData.getGroupTranslations() != null) {
                for (var t : backupData.getGroupTranslations()) {
                    if (t.getGroup() != null && t.getGroup().getId() != null) {
                        t.setGroup(session.getReference(es.brasatech.fastbite.jpa.group.GroupEntity.class, t.getGroup().getId()));
                    }
                }
            }

            if (backupData.getProductTranslations() != null) {
                for (var t : backupData.getProductTranslations()) {
                    if (t.getProduct() != null && t.getProduct().getId() != null) {
                        t.setProduct(session.getReference(es.brasatech.fastbite.jpa.product.ProductEntity.class, t.getProduct().getId()));
                    }
                }
            }

            if (backupData.getCustomizationTranslations() != null) {
                for (var t : backupData.getCustomizationTranslations()) {
                    if (t.getCustomization() != null && t.getCustomization().getId() != null) {
                        t.setCustomization(session.getReference(es.brasatech.fastbite.jpa.customization.CustomizationEntity.class, t.getCustomization().getId()));
                    }
                }
            }

            if (backupData.getCustomizationOptionTranslations() != null) {
                for (var t : backupData.getCustomizationOptionTranslations()) {
                    if (t.getCustomizationOption() != null && t.getCustomizationOption().getId() != null) {
                        t.setCustomizationOption(session.getReference(es.brasatech.fastbite.jpa.customization.CustomizationOptionEntity.class, t.getCustomizationOption().getId()));
                    }
                }
            }

            if (backupData.getDiscountRuleTranslations() != null) {
                for (var t : backupData.getDiscountRuleTranslations()) {
                    if (t.getDiscountRule() != null && t.getDiscountRule().getId() != null) {
                        t.setDiscountRule(session.getReference(es.brasatech.fastbite.jpa.discount.DiscountRuleEntity.class, t.getDiscountRule().getId()));
                    }
                }
            }

            if (backupData.getTableTranslations() != null) {
                for (var t : backupData.getTableTranslations()) {
                    if (t.getTable() != null && t.getTable().getId() != null) {
                        t.setTable(session.getReference(es.brasatech.fastbite.jpa.table.TableEntity.class, t.getTable().getId()));
                    }
                }
            }

            // 4. Populate database (ordered to satisfy dependencies using Hibernate replicate)
            replicateAll(backupData.getGroups());
            replicateAll(backupData.getProducts());
            replicateAll(backupData.getCustomizations());
            replicateAll(backupData.getTables());
            replicateAll(backupData.getDiscountRules());
            replicateAll(backupData.getOrders());
            replicateAll(backupData.getUsers());
            replicateAll(backupData.getPaymentConfigs());

            replicateAll(backupData.getGroupTranslations());
            replicateAll(backupData.getProductTranslations());
            replicateAll(backupData.getCustomizationTranslations());
            replicateAll(backupData.getCustomizationOptionTranslations());
            replicateAll(backupData.getDiscountRuleTranslations());
            replicateAll(backupData.getTableTranslations());

            log.info("Successfully restored backup data for tenant: {}", tenantId);

        } catch (Exception e) {
            log.error("Failed to restore backup for tenant: " + tenantId, e);
            throw new RuntimeException("Restore backup failed: " + e.getMessage(), e);
        } finally {
            if (originalTenant != null) {
                TenantContext.setCurrentTenant(originalTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void cleanTenantData(String tenantId) {
        String originalTenant = TenantContext.getCurrentTenant();
        try {
            TenantContext.setCurrentTenant(tenantId);

            // 1. Delete all database records (reverse dependency order using native queries to handle element collections)
            entityManager.createNativeQuery("DELETE FROM table_orders").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM table_translations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM dining_tables").executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM cart_item_customizations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM cart_items").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM orders").executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM group_products").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM group_translations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM groups").executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM product_customizations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM product_translations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM products").executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM customization_option_translations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM customization_options").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM customization_translations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM customizations").executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM discount_rule_translations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM discount_rules").executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM user_roles").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM payment_modes").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM money_denominations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM payment_configs").executeUpdate();

            entityManager.flush();
            entityManager.clear();

            // 2. Delete tenant image directory files
            Path tenantMediaPath = Paths.get(uploadDirectory).resolve(tenantId);
            if (Files.exists(tenantMediaPath) && Files.isDirectory(tenantMediaPath)) {
                Files.walk(tenantMediaPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            }

            log.info("Successfully cleaned all data and media files for tenant: {}", tenantId);

        } catch (Exception e) {
            log.error("Failed to clean data for tenant: " + tenantId, e);
            throw new RuntimeException("Clean tenant data failed", e);
        } finally {
            if (originalTenant != null) {
                TenantContext.setCurrentTenant(originalTenant);
            } else {
                TenantContext.clear();
            }
        }
    }



    private void replicateAll(List<?> entities) {
        if (entities == null) return;
        org.hibernate.Session session = entityManager.unwrap(org.hibernate.Session.class);
        for (Object entity : entities) {
            session.replicate(entity, org.hibernate.ReplicationMode.OVERWRITE);
        }
    }

    // Inner DTO helper for serialization
    @lombok.Data
    public static class TenantBackupData {
        private List<GroupEntity> groups = new ArrayList<>();
        private List<ProductEntity> products = new ArrayList<>();
        private List<CustomizationEntity> customizations = new ArrayList<>();
        private List<CustomizationOptionEntity> customizationOptions = new ArrayList<>();
        private List<TableEntity> tables = new ArrayList<>();
        private List<DiscountRuleEntity> discountRules = new ArrayList<>();
        private List<OrderEntity> orders = new ArrayList<>();
        private List<UserEntity> users = new ArrayList<>();
        private List<PaymentConfigEntity> paymentConfigs = new ArrayList<>();

        private List<GroupTranslationEntity> groupTranslations = new ArrayList<>();
        private List<ProductTranslationEntity> productTranslations = new ArrayList<>();
        private List<CustomizationTranslationEntity> customizationTranslations = new ArrayList<>();
        private List<CustomizationOptionTranslationEntity> customizationOptionTranslations = new ArrayList<>();
        private List<DiscountRuleTranslationEntity> discountRuleTranslations = new ArrayList<>();
        private List<TableTranslationEntity> tableTranslations = new ArrayList<>();
    }
}
