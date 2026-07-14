package es.brasatech.fastbite.jpa.tenant;

import es.brasatech.fastbite.domain.tenant.TenantContext;
import es.brasatech.fastbite.jpa.customization.CustomizationEntity;
import es.brasatech.fastbite.jpa.customization.CustomizationJpaRepository;
import es.brasatech.fastbite.jpa.group.GroupEntity;
import es.brasatech.fastbite.jpa.group.GroupJpaRepository;
import es.brasatech.fastbite.jpa.product.ProductEntity;
import es.brasatech.fastbite.jpa.product.ProductJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = es.brasatech.fastbite.jpa.TestConfig.class)
@ActiveProfiles("jpa")
class TenantBackupRestoreAdapterTest {

    @Autowired
    private TenantBackupRestoreAdapter backupRestoreAdapter;

    @Autowired
    private TenantProvisionerAdapter tenantProvisionerAdapter;

    @Autowired
    private GroupJpaRepository groupRepository;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private CustomizationJpaRepository customizationRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testBackupCleanRestoreLifecycle() {
        String testTenant = "backup_test_tenant";
        tenantProvisionerAdapter.provisionTenant(testTenant);
        TenantContext.setCurrentTenant(testTenant);

        // 1. Populate test data under transaction
        transactionTemplate.execute(status -> {
            // Add customization
            CustomizationEntity c = new CustomizationEntity();
            c.setName("Extra Cheese");
            c.setType("radio");
            customizationRepository.save(c);

            // Add product
            ProductEntity p = new ProductEntity();
            p.setName("Double Cheeseburger");
            p.setPrice(BigDecimal.valueOf(11.50));
            p.setActive(true);
            productRepository.save(p);

            // Add group
            GroupEntity g = new GroupEntity();
            g.setName("Burgers Menu");
            g.setDescription("Beef burgers");
            g.setProducts(List.of(p.getId()));
            groupRepository.save(g);

            return null;
        });

        // 2. Export backup
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        backupRestoreAdapter.exportBackup(testTenant, baos);
        byte[] backupBytes = baos.toByteArray();
        assertTrue(backupBytes.length > 0);

        // 3. Clean tenant data
        backupRestoreAdapter.cleanTenantData(testTenant);

        // 4. Verify data is gone
        TenantContext.setCurrentTenant(testTenant);
        transactionTemplate.execute(status -> {
            assertEquals(0, groupRepository.count());
            assertEquals(0, productRepository.count());
            assertEquals(0, customizationRepository.count());
            return null;
        });

        // 5. Restore data
        ByteArrayInputStream bais = new ByteArrayInputStream(backupBytes);
        backupRestoreAdapter.importRestore(testTenant, bais);

        // 6. Verify data is fully restored
        TenantContext.setCurrentTenant(testTenant);
        transactionTemplate.execute(status -> {
            List<GroupEntity> restoredGroups = groupRepository.findAll();
            assertEquals(1, restoredGroups.size());
            assertEquals("Burgers Menu", restoredGroups.get(0).getName());

            List<ProductEntity> restoredProducts = productRepository.findAll();
            assertEquals(1, restoredProducts.size());
            assertEquals("Double Cheeseburger", restoredProducts.get(0).getName());

            List<CustomizationEntity> restoredCustoms = customizationRepository.findAll();
            assertEquals(1, restoredCustoms.size());
            assertEquals("Extra Cheese", restoredCustoms.get(0).getName());

            return null;
        });
    }
}
