package es.brasatech.fastbite.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Public areas
                        .requestMatchers("/", "/menu/**", "/api/calculate-cart", "/api/calculate-confirmation",
                                "/api/create-order", "/order-confirmation/**", "/select-payment", "/signup").permitAll()
                        .requestMatchers("/t/*/menu/**", "/t/*/api/calculate-cart", "/t/*/api/calculate-confirmation",
                                "/t/*/api/create-order", "/t/*/order-confirmation/**", "/t/*/select-payment").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/user-images/**").permitAll()
                        .requestMatchers("/login", "/error", "/t/*/login").permitAll()

                        // Dashboard access (all staff roles)
                        .requestMatchers("/dashboard/**", "/counter/**", "/api/order/**", "/api/counter/**",
                                "/t/*/dashboard/**", "/t/*/counter/**", "/t/*/api/order/**", "/t/*/api/counter/**")
                        .hasAnyRole("ADMIN", "MANAGER", "CASHIER", "COOK", "WAITER")

                        // BackOffice access (admin and manager only)
                        .requestMatchers("/backoffice/**", "/api/backoffice/**", "/api/backoffice/orders/reassign-table",
                                "/t/*/backoffice/**", "/t/*/api/backoffice/**").hasAnyRole("ADMIN", "MANAGER")

                        // Everything else requires authentication
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll())
                .csrf(csrf -> {
                    // Enable CSRF (default)
                })
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
