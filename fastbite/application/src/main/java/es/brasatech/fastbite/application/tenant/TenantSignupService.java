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

        // 2. Swapping context to save the admin user under the new tenant schema
        try {
            TenantContext.setCurrentTenant(tenantId);

            UserDto adminUser = new UserDto(
                    null,
                    username,
                    encodedPassword,
                    fullName,
                    Set.of(Role.ADMIN),
                    true
            );

            userService.save(adminUser);
            LOGGER.info("Admin user '" + username + "' created for tenant: " + tenantId);
        } finally {
            TenantContext.clear();
        }
    }
}
