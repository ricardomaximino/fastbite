package es.brasatech.fastbite.security;

import es.brasatech.fastbite.application.office.UserService;
import es.brasatech.fastbite.domain.user.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername called for username: {}", username);
        try {
            Optional<UserDto> userOpt = userService.findByUsername(username);
            log.info("User lookup result: {}", userOpt.isPresent());
            if (userOpt.isPresent()) {
                log.info("User found. Roles: {}", userOpt.get().roles());
            }
            return userOpt
                    .map(user -> User.builder()
                            .username(user.username())
                            .password(user.password())
                            .disabled(!user.active())
                            .authorities(user.roles().stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                                    .collect(Collectors.toSet()))
                            .build())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        } catch (Exception e) {
            log.error("Exception loading user: " + username, e);
            throw new UsernameNotFoundException("Error loading user: " + username, e);
        }
    }
}
