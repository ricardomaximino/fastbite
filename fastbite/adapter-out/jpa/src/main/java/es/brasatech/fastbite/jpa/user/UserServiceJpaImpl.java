package es.brasatech.fastbite.jpa.user;

import es.brasatech.fastbite.application.office.UserService;
import es.brasatech.fastbite.domain.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Profile("jpa")
@RequiredArgsConstructor
public class UserServiceJpaImpl implements UserService {

    private final UserJpaRepository userRepository;

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public void save(UserDto userDto) {
        UserEntity entity = userRepository.findByUsername(userDto.username())
                .orElse(new UserEntity());

        entity.setUsername(userDto.username());
        entity.setPassword(userDto.password());
        entity.setFullName(userDto.fullName());
        entity.setRoles(userDto.roles());
        entity.setActive(userDto.active());

        userRepository.save(entity);
    }

    @Override
    public boolean existsAny() {
        return userRepository.count() > 0;
    }

    private UserDto toDto(UserEntity entity) {
        return new UserDto(
                entity.getId(),
                entity.getUsername(),
                entity.getPassword(),
                entity.getFullName(),
                entity.getRoles(),
                entity.isActive());
    }
}
