package es.brasatech.fastbite.domain.user;

import java.util.Set;

/**
 * Data Transfer Object for User data.
 */
public record UserDto(
                String id,
                String username,
                String password,
                String fullName,
                Set<Role> roles,
                boolean active,
                String tenantId) implements java.io.Serializable {

    public boolean isOwner() {
        return roles != null && roles.contains(Role.OWNER);
    }

    public String getPrimaryRoleName() {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        return roles.iterator().next().name();
    }
}
