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
public class SecurityDataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // Initialize kebab tenant users
        try {
            es.brasatech.fastbite.domain.tenant.TenantContext.setCurrentTenant("kebab");
            if (!userService.existsAny()) {
                log.info("No users found in default tenant. Creating default staff users...");
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
