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
        if (!userService.existsAny()) {
            log.info("No users found. Creating default staff users...");

            createUser("admin", "Admin User", Role.ADMIN);
            createUser("manager", "Store Manager", Role.MANAGER);
            createUser("cashier", "Cashier Staff", Role.CASHIER);
            createUser("cook", "Kitchen Staff", Role.COOK);
            createUser("waiter", "Service Staff", Role.WAITER);

            log.info("Default users created successfully.");
        }
    }

    private void createUser(String username, String fullName, Role role) {
        UserDto user = new UserDto(
                null,
                username,
                passwordEncoder.encode("password"),
                fullName,
                Set.of(role),
                true);
        userService.save(user);
        log.info("Created user: {} with role: {}", username, role);
    }
}
