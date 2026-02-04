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
                boolean active) {
}
