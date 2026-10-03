package es.brasatech.fastbite.application.tenant;

import java.util.Locale;
import java.util.Set;

public final class TenantRegistrationRules {
    private static final Set<String> RESERVED = Set.of("default", "admin", "kebab", "signup", "login",
            "logout", "owner", "menu", "dashboard", "counter", "backoffice", "api", "css", "js", "images",
            "webjars", "stripe", "error", "actuator", "appspecific");
    private TenantRegistrationRules() { }

    public static String tenantId(String input) {
        String tenant = input == null ? "" : input.toLowerCase(Locale.ROOT);
        if (!tenant.matches("[a-z0-9]{1,56}") || RESERVED.contains(tenant)) {
            throw new IllegalArgumentException("Choose an available restaurant identifier of 1 to 56 letters or digits.");
        }
        return tenant;
    }

    public static void owner(String username) {
        if (username == null || !username.matches("[a-zA-Z0-9_.@-]{1,100}")) {
            throw new IllegalArgumentException("Invalid owner username.");
        }
    }

    public static void plan(String plan) {
        if (!java.util.Set.of("RESTAURANT", "Free Demo").contains(plan == null ? "" : plan)) throw new IllegalArgumentException("Invalid restaurant plan.");
    }
}
