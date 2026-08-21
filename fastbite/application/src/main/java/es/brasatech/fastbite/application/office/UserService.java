package es.brasatech.fastbite.application.office;

import es.brasatech.fastbite.domain.user.UserDto;

import java.util.List;
import java.util.Optional;

/**
 * Service for user management.
 */
public interface UserService {
    Optional<UserDto> findByUsername(String username);

    void save(UserDto userDto);

    boolean existsAny();

    List<UserDto> findAll();

    void delete(String id);
}
