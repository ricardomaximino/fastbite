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
    private final TenantLocationService tenantLocationService;

    public TenantSignupService(TenantProvisionerPort tenantProvisionerPort, UserService userService, TenantLocationService tenantLocationService) {
        this.tenantProvisionerPort = tenantProvisionerPort;
        this.userService = userService;
        this.tenantLocationService = tenantLocationService;
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
                    Set.of(Role.OWNER, Role.ADMIN),
                    true,
                    tenantId
            );
            userService.save(ownerUser);
            LOGGER.info("SaaS Tenant Owner user '" + username + "' created in master schema for tenant: " + tenantId);
        } catch (Exception e) {
            LOGGER.severe("Failed to create SaaS Tenant Owner in master schema: " + e.getMessage());
        }

        // 3. Register location association in tenant_locations registry
        try {
            tenantLocationService.registerLocation(username, tenantId, "Free Demo");
            LOGGER.info("Successfully registered location '" + tenantId + "' for owner '" + username + "'");
        } catch (Exception e) {
            LOGGER.severe("Failed to register location mapping: " + e.getMessage());
        }
    }

    public void registerAdditionalLocation(String tenantId, String ownerUsername, String plan) {
        if (tenantId == null || !tenantId.matches("^[a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("Invalid tenant identifier. Only alphanumeric characters are allowed.");
        }

        if ("default".equalsIgnoreCase(tenantId) || "admin".equalsIgnoreCase(tenantId)) {
            throw new IllegalArgumentException("Reserved tenant identifier.");
        }

        LOGGER.info("Starting registration of additional location '" + tenantId + "' for owner: " + ownerUsername + " with plan: " + plan);

        // 1. Provision database schema and initial tables
        tenantProvisionerPort.provisionTenant(tenantId);

        // 2. Register location association in tenant_locations registry
        tenantLocationService.registerLocation(ownerUsername, tenantId, plan);
        LOGGER.info("Successfully registered additional location '" + tenantId + "' for owner '" + ownerUsername + "'");
    }
}
