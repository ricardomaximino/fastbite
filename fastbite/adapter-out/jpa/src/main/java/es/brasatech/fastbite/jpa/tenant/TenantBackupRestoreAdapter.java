package es.brasatech.fastbite.jpa.tenant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.brasatech.fastbite.application.tenant.TenantBackupRestorePort;
import es.brasatech.fastbite.domain.tenant.TenantContext;
import es.brasatech.fastbite.jpa.customization.*;
import es.brasatech.fastbite.jpa.discount.DiscountRuleEntity;
import es.brasatech.fastbite.jpa.discount.DiscountRuleJpaRepository;
import es.brasatech.fastbite.jpa.group.GroupEntity;
import es.brasatech.fastbite.jpa.group.GroupJpaRepository;
import es.brasatech.fastbite.jpa.order.OrderEntity;
import es.brasatech.fastbite.jpa.order.OrderJpaRepository;
import es.brasatech.fastbite.jpa.payment.PaymentConfigEntity;
import es.brasatech.fastbite.jpa.payment.PaymentConfigJpaRepository;
import es.brasatech.fastbite.jpa.product.ProductEntity;
import es.brasatech.fastbite.jpa.product.ProductJpaRepository;
import es.brasatech.fastbite.jpa.table.TableEntity;
import es.brasatech.fastbite.jpa.table.TableJpaRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
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
                    String entryName = entry.getName().replace("\\", "/");
                    if ("data.json".equalsIgnoreCase(entryName) || entryName.endsWith("/data.json")) {
                        // Jackson reads data.json
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }
                        backupData = objectMapper.readValue(baos.toByteArray(), TenantBackupData.class);
                    } else if (entryName.startsWith("media/") && !entry.isDirectory()) {
                        String relativeFileName = entryName.substring("media/".length());
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

            // Rewrite product image URLs to match the new tenant ID
            if (backupData.getProducts() != null) {
                for (var prod : backupData.getProducts()) {
                    if (prod.getImage() != null) {
                        prod.setImage(rewriteImageUrl(prod.getImage(), tenantId));
                    }
                }
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

            // Backups made before translations moved into each row carry them as separate rows
            applyLegacyTranslations(backupData.getGroups(), backupData.getGroupTranslations(), "group",
                    GroupEntity::getId, GroupEntity::setTranslations);
            applyLegacyTranslations(backupData.getProducts(), backupData.getProductTranslations(), "product",
                    ProductEntity::getId, ProductEntity::setTranslations);
            applyLegacyTranslations(backupData.getCustomizations(), backupData.getCustomizationTranslations(), "customization",
                    CustomizationEntity::getId, CustomizationEntity::setTranslations);
            List<CustomizationOptionEntity> options = new ArrayList<>(backupData.getCustomizationOptions());
            backupData.getCustomizations().forEach(c -> options.addAll(c.getOptions()));
            applyLegacyTranslations(options, backupData.getCustomizationOptionTranslations(), "customizationOption",
                    CustomizationOptionEntity::getId, CustomizationOptionEntity::setTranslations);
            applyLegacyTranslations(backupData.getDiscountRules(), backupData.getDiscountRuleTranslations(), "discountRule",
                    DiscountRuleEntity::getId, DiscountRuleEntity::setTranslations);
            applyLegacyTranslations(backupData.getTables(), backupData.getTableTranslations(), "table",
                    TableEntity::getId, TableEntity::setTranslations);

            // 4. Populate database (ordered to satisfy dependencies using Hibernate replicate)
            replicateAll(backupData.getGroups());
            replicateAll(backupData.getProducts());
            replicateAll(backupData.getCustomizations());
            replicateAll(backupData.getTables());
            replicateAll(backupData.getDiscountRules());
            replicateAll(backupData.getOrders());
            replicateAll(backupData.getUsers());
            replicateAll(backupData.getPaymentConfigs());

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
            entityManager.createNativeQuery("DELETE FROM dining_tables").executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM cart_item_customizations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM cart_items").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM orders").executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM group_products").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM groups").executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM product_customizations").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM products").executeUpdate();
            
            entityManager.createNativeQuery("DELETE FROM customization_options").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM customizations").executeUpdate();
            
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

        // Legacy: translations as separate rows, only present in backups made before translations
        // moved into each entity. Read on restore, never written.
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<Map<String, Object>> groupTranslations = new ArrayList<>();
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<Map<String, Object>> productTranslations = new ArrayList<>();
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<Map<String, Object>> customizationTranslations = new ArrayList<>();
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<Map<String, Object>> customizationOptionTranslations = new ArrayList<>();
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<Map<String, Object>> discountRuleTranslations = new ArrayList<>();
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<Map<String, Object>> tableTranslations = new ArrayList<>();
    }

    /** Merges legacy translation rows ({parentKey: {id}, language, name, description}) into their parent entities. */
    private static <E> void applyLegacyTranslations(List<E> entities, List<Map<String, Object>> rows, String parentKey,
                                                    Function<E, String> id,
                                                    BiConsumer<E, Map<String, Map<String, String>>> setTranslations) {
        if (entities == null || rows == null || rows.isEmpty()) {
            return;
        }
        Map<String, Map<String, Map<String, String>>> byParent = new HashMap<>();
        for (Map<String, Object> row : rows) {
            if (row.get(parentKey) instanceof Map<?, ?> parent && row.get("language") instanceof String language) {
                Map<String, String> fields = byParent.computeIfAbsent(String.valueOf(parent.get("id")), k -> new HashMap<>())
                        .computeIfAbsent(language, k -> new HashMap<>());
                for (String field : List.of("name", "description")) {
                    if (row.get(field) instanceof String value && !value.isBlank()) {
                        fields.put(field, value);
                    }
                }
            }
        }
        for (E entity : entities) {
            Map<String, Map<String, String>> translations = byParent.get(id.apply(entity));
            if (translations != null) {
                setTranslations.accept(entity, translations);
            }
        }
    }

    private String rewriteImageUrl(String originalUrl, String newTenantId) {
        if (originalUrl == null) return null;
        if (originalUrl.startsWith("/user-images/")) {
            String remainder = originalUrl.substring("/user-images/".length());
            int nextSlash = remainder.indexOf('/');
            if (nextSlash != -1) {
                return "/user-images/" + newTenantId + remainder.substring(nextSlash);
            }
        }
        return originalUrl;
    }
}
