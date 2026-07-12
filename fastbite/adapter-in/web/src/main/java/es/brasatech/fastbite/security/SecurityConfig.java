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
                                "/api/create-order", "/order-confirmation/**", "/select-payment", "/signup", "/api/webhooks/stripe").permitAll()
                        .requestMatchers("/*/menu/**", "/*/api/calculate-cart", "/*/api/calculate-confirmation",
                                "/*/api/create-order", "/*/order-confirmation/**", "/*/select-payment").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/user-images/**").permitAll()
                        .requestMatchers("/login", "/error", "/*/login").permitAll()

                        // Dashboard access (all staff roles)
                        .requestMatchers("/dashboard/**", "/counter/**", "/api/order/**", "/api/counter/**",
                                "/*/dashboard/**", "/*/counter/**", "/*/api/order/**", "/*/api/counter/**")
                        .hasAnyRole("ADMIN", "MANAGER", "CASHIER", "COOK", "WAITER")

                        // BackOffice access (admin and manager only)
                        .requestMatchers("/backoffice/**", "/api/backoffice/**", "/api/backoffice/orders/reassign-table",
                                "/*/backoffice/**", "/*/api/backoffice/**").hasAnyRole("ADMIN", "MANAGER")

                        // Everything else requires authentication
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(new TenantAuthenticationSuccessHandler())
                        .failureHandler(new TenantAuthenticationFailureHandler())
                        .permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new TenantAuthenticationEntryPoint()))
                .logout(logout -> logout
                        .logoutRequestMatcher(request -> {
                            String path = request.getRequestURI().substring(request.getContextPath().length());
                            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                                return false;
                            }
                            if (path.equals("/logout")) {
                                return true;
                            }
                            String[] segments = path.split("/");
                            if (segments.length > 2 && "logout".equals(segments[segments.length - 1])) {
                                String tenantId = segments[1];
                                return !es.brasatech.fastbite.config.TenantInterceptor.isReserved(tenantId);
                            }
                            return false;
                        })
                        .logoutSuccessHandler(new TenantLogoutSuccessHandler())
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/webhooks/stripe")
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Custom Entry Point to redirect to /{tenantId}/login instead of global /login
    private static class TenantAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {
        @Override
        public void commence(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws java.io.IOException {
            String uri = request.getRequestURI();
            String contextPath = request.getContextPath();
            String path = uri.substring(contextPath.length());
            String[] segments = path.split("/");
            if (segments.length > 1) {
                String firstSegment = segments[1];
                if (!es.brasatech.fastbite.config.TenantInterceptor.isReserved(firstSegment)) {
                    response.sendRedirect(contextPath + "/" + firstSegment + "/login");
                    return;
                }
            }
            response.sendRedirect(contextPath + "/login");
        }
    }

    // Custom Success Handler to redirect to /{tenantId}/menu after successful login
    private static class TenantAuthenticationSuccessHandler extends org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler {
        @Override
        public void onAuthenticationSuccess(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, org.springframework.security.core.Authentication authentication) throws java.io.IOException, jakarta.servlet.ServletException {
            String tenantId = (String) request.getAttribute("tenantId");
            if (tenantId == null) {
                tenantId = es.brasatech.fastbite.domain.tenant.TenantContext.getCurrentTenant();
            }
            if (tenantId == null) {
                String path = request.getRequestURI().substring(request.getContextPath().length());
                String[] segments = path.split("/");
                if (segments.length > 1 && !es.brasatech.fastbite.config.TenantInterceptor.isReserved(segments[1])) {
                    tenantId = segments[1];
                }
            }
            if (tenantId != null) {
                setDefaultTargetUrl("/" + tenantId + "/menu");
            } else {
                setDefaultTargetUrl("/");
            }

            // Sanitize RequestCache to prevent devtools or background JSON endpoints from capturing post-login redirect target
            org.springframework.security.web.savedrequest.RequestCache requestCache = new org.springframework.security.web.savedrequest.HttpSessionRequestCache();
            org.springframework.security.web.savedrequest.SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null) {
                String redirectUrl = savedRequest.getRedirectUrl();
                if (redirectUrl.contains("com.chrome.devtools") || redirectUrl.contains("/appspecific/") || redirectUrl.endsWith(".json") || redirectUrl.contains("/favicon.ico")) {
                    requestCache.removeRequest(request, response);
                }
            }

            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

    // Custom Logout Success Handler to redirect back to /{tenantId}/menu on logout
    private static class TenantLogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {
        @Override
        public void onLogoutSuccess(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, org.springframework.security.core.Authentication authentication) throws java.io.IOException {
            String tenantId = (String) request.getAttribute("tenantId");
            if (tenantId == null) {
                tenantId = es.brasatech.fastbite.domain.tenant.TenantContext.getCurrentTenant();
            }
            if (tenantId == null) {
                String path = request.getRequestURI().substring(request.getContextPath().length());
                String[] segments = path.split("/");
                if (segments.length > 1 && !es.brasatech.fastbite.config.TenantInterceptor.isReserved(segments[1])) {
                    tenantId = segments[1];
                }
            }
            if (tenantId != null) {
                response.sendRedirect(request.getContextPath() + "/" + tenantId + "/menu");
            } else {
                response.sendRedirect(request.getContextPath() + "/signup");
            }
        }
    }

    // Custom Failure Handler to redirect back to /{tenantId}/login?error on login failure
    private static class TenantAuthenticationFailureHandler extends org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler {
        @Override
        public void onAuthenticationFailure(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, org.springframework.security.core.AuthenticationException exception) throws java.io.IOException, jakarta.servlet.ServletException {
            String tenantId = (String) request.getAttribute("tenantId");
            if (tenantId == null) {
                tenantId = es.brasatech.fastbite.domain.tenant.TenantContext.getCurrentTenant();
            }
            if (tenantId == null) {
                String path = request.getRequestURI().substring(request.getContextPath().length());
                String[] segments = path.split("/");
                if (segments.length > 1 && !es.brasatech.fastbite.config.TenantInterceptor.isReserved(segments[1])) {
                    tenantId = segments[1];
                }
            }
            if (tenantId != null) {
                saveException(request, exception);
                getRedirectStrategy().sendRedirect(request, response, "/" + tenantId + "/login?error");
            } else {
                setDefaultFailureUrl("/login?error");
                super.onAuthenticationFailure(request, response, exception);
            }
        }
    }
}
