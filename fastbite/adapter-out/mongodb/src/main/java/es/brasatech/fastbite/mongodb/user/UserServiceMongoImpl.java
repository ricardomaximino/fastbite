package es.brasatech.fastbite.mongodb.user;

import es.brasatech.fastbite.application.office.UserService;
import es.brasatech.fastbite.domain.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Profile("mongodb")
@RequiredArgsConstructor
public class UserServiceMongoImpl implements UserService {

    private final UserMongoRepository userRepository;

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toDto);
    }

    @Override
    public void save(UserDto userDto) {
        UserDocument document = userRepository.findByUsername(userDto.username())
                .orElse(new UserDocument());

        document.setUsername(userDto.username());
        document.setPassword(userDto.password());
        document.setFullName(userDto.fullName());
        document.setRoles(userDto.roles());
        document.setActive(userDto.active());

        userRepository.save(document);
    }

    @Override
    public boolean existsAny() {
        return userRepository.count() > 0;
    }

    private UserDto toDto(UserDocument document) {
        return new UserDto(
                document.getId(),
                document.getUsername(),
                document.getPassword(),
                document.getFullName(),
                document.getRoles(),
                document.isActive());
    }
}
