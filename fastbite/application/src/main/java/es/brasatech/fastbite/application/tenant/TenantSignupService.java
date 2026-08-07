package es.brasatech.fastbite.application.tenant;

import es.brasatech.fastbite.application.office.UserService;
import es.brasatech.fastbite.domain.tenant.TenantContext;
import es.brasatech.fastbite.domain.user.Role;
import es.brasatech.fastbite.domain.user.UserDto;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.logging.Logger;

@Service
public class TenantSignupService {

    private static final Logger LOGGER = Logger.getLogger(TenantSignupService.class.getName());

    private final TenantProvisionerPort tenantProvisionerPort;
    private final UserService userService;

    public TenantSignupService(TenantProvisionerPort tenantProvisionerPort, UserService userService) {
        this.tenantProvisionerPort = tenantProvisionerPort;
        this.userService = userService;
    }

    public void registerTenant(String tenantId, String username, String encodedPassword, String fullName) {
        if (tenantId == null || !tenantId.matches("^[a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("Invalid tenant identifier. Only alphanumeric characters are allowed.");
        }

        if ("default".equalsIgnoreCase(tenantId) || "admin".equalsIgnoreCase(tenantId)) {
            throw new IllegalArgumentException("Reserved tenant identifier.");
        }

        LOGGER.info("Starting registration for tenant: " + tenantId);

        // 1. Provision database schema and initial tables
        tenantProvisionerPort.provisionTenant(tenantId);

        // 2. Save the SaaS Tenant Owner in the master schema (PUBLIC)
        try {
            TenantContext.clear(); // Maps to PUBLIC
            UserDto ownerUser = new UserDto(
                    null,
                    username,
                    encodedPassword,
                    fullName,
                    Set.of(Role.OWNER),
                    true,
                    tenantId
            );
            userService.save(ownerUser);
            LOGGER.info("SaaS Tenant Owner user '" + username + "' created in master schema for tenant: " + tenantId);
        } catch (Exception e) {
            LOGGER.severe("Failed to create SaaS Tenant Owner in master schema: " + e.getMessage());
        }

        // 3. Swapping context to save the operational admin user under the new tenant schema
        try {
            TenantContext.setCurrentTenant(tenantId);

            UserDto adminUser = new UserDto(
                    null,
                    "admin",
                    encodedPassword, // Keep same password for easy onboarding
                    fullName,
                    Set.of(Role.ADMIN),
                    true,
                    tenantId
            );

            userService.save(adminUser);
            LOGGER.info("Operational admin user 'admin' created for tenant: " + tenantId);
        } finally {
            TenantContext.clear();
        }
    }
}
