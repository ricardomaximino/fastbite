package es.brasatech.fastbite.security;

import es.brasatech.fastbite.application.office.UserService;
import es.brasatech.fastbite.domain.user.Role;
import es.brasatech.fastbite.domain.user.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.core.annotation.Order(2)
public class SecurityDataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final es.brasatech.fastbite.application.tenant.TenantLocationService tenantLocationService;

    @Override
    public void run(String... args) {
        // Initialize kebabowner in PUBLIC schema and associate it with kebab location
        try {
            es.brasatech.fastbite.domain.tenant.TenantContext.clear(); // Ensure we are in PUBLIC
            if (userService.findByUsername("kebabowner").isEmpty()) {
                log.info("Creating default SaaS Tenant Owner: kebabowner...");
                UserDto owner = new UserDto(
                        null,
                        "kebabowner",
                        passwordEncoder.encode("password"),
                        "Kebab Owner",
                        new java.util.HashSet<>(Set.of(Role.OWNER, Role.ADMIN)),
                        true,
                        "kebab"
                );
                userService.save(owner);
                
                // Associate location
                if (tenantLocationService.getLocation("kebab").isEmpty()) {
                    tenantLocationService.registerLocation("kebabowner", "kebab", "Free Demo");
                }
            }
        } catch (Exception e) {
            log.error("Failed to seed default kebabowner: " + e.getMessage(), e);
        }

        // Initialize kebab tenant users
        try {
            es.brasatech.fastbite.domain.tenant.TenantContext.setCurrentTenant("kebab");
            if (userService.findByUsername("admin").isEmpty()) {
                log.info("No admin user found in default tenant. Creating default staff users...");
                createUser("admin", "Admin User", "password", Role.ADMIN);
                createUser("manager", "Store Manager", "password", Role.MANAGER);
                createUser("cashier", "Cashier Staff", "password", Role.CASHIER);
                createUser("cook", "Kitchen Staff", "password", Role.COOK);
                createUser("waiter", "Service Staff", "password", Role.WAITER);
            }
        } finally {
            es.brasatech.fastbite.domain.tenant.TenantContext.clear();
        }
    }

    private void createUser(String username, String fullName, String rawPassword, Role role) {
        String tenantId = es.brasatech.fastbite.domain.tenant.TenantContext.getCurrentTenant();
        UserDto user = new UserDto(
                null,
                username,
                passwordEncoder.encode(rawPassword),
                fullName,
                new java.util.HashSet<>(Set.of(role)),
                true,
                tenantId);
        userService.save(user);
        log.info("Created user: {} with role: {}", username, role);
    }
}
