package es.brasatech.fastbite.security;

import es.brasatech.fastbite.application.office.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.findByUsername(username)
                .map(user -> User.builder()
                        .username(user.username())
                        .password(user.password())
                        .disabled(!user.active())
                        .authorities(user.roles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                                .collect(Collectors.toSet()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
