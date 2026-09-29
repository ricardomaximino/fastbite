package es.brasatech.fastbite.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.Collection;

/**
 * A signed-in user together with the restaurant the account belongs to.
 * Platform accounts (restaurant owners, stored in the platform schema) have no home tenant.
 */
public class TenantUser extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String homeTenantId;

    public TenantUser(String username, String password, boolean enabled,
                      Collection<? extends GrantedAuthority> authorities, String homeTenantId) {
        super(username, password, enabled, true, true, true, authorities);
        this.homeTenantId = homeTenantId;
    }

    public String getHomeTenantId() {
        return homeTenantId;
    }

    public boolean isPlatformAccount() {
        return homeTenantId == null;
    }

    public boolean belongsTo(String tenantId) {
        return homeTenantId != null && homeTenantId.equalsIgnoreCase(tenantId);
    }
}
