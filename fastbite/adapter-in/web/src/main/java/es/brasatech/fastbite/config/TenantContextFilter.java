package es.brasatech.fastbite.config;

import es.brasatech.fastbite.domain.tenant.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class TenantContextFilter implements Filter {

    private final TenantRoutingResolver tenantResolver;

    public TenantContextFilter(TenantRoutingResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantId = null;

        // 1. Resolve from URL path first to check if it's a reserved platform path
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());
        String[] segments = path.split("/");
        boolean isReservedPath = false;
        if (segments.length > 1) {
            String firstSegment = segments[1];
            if (TenantInterceptor.isReserved(firstSegment)) {
                isReservedPath = true;
            } else {
                tenantId = firstSegment;
            }
        }

        // 2. Only resolve subdomain from Host if not a reserved platform path
        if (!isReservedPath) {
            if (tenantId == null) {
                String host = httpRequest.getHeader("Host");
                tenantId = tenantResolver.resolveTenantId(host);
            }
        }

        // 3. Fallbacks (parameter, referer) are resolved even for API/reserved requests
        if (tenantId == null) {
            tenantId = httpRequest.getParameter("tenantId");
        }

        // Fallback: Resolve tenant from Referer header (e.g. AJAX requests or /login POST)
        boolean skipReferer = isReservedPath && (
            path.startsWith("/login") || 
            path.startsWith("/signup") || 
            path.startsWith("/logout") || 
            path.equals("/")
        );
        if (tenantId == null && !skipReferer) {
            String referer = httpRequest.getHeader("Referer");
            if (referer != null) {
                try {
                    java.net.URI refererUri = new java.net.URI(referer);
                    String refererPath = refererUri.getPath();
                    if (refererPath != null && refererPath.startsWith("/")) {
                        String[] segmentsRef = refererPath.split("/");
                        if (segmentsRef.length > 1) {
                            String firstSegment = segmentsRef[1];
                            if (!TenantInterceptor.isReserved(firstSegment)) {
                                tenantId = firstSegment;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore malformed referer
                }
            }
        }

        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
            
            // SSO Auto-Authorization: If owner is logged in and owns this tenant, grant them ADMIN role temporarily
            try {
                jakarta.servlet.http.HttpSession session = httpRequest.getSession(false);
                if (session != null) {
                    org.springframework.security.core.context.SecurityContext securityContext = 
                        (org.springframework.security.core.context.SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
                    if (securityContext != null) {
                        org.springframework.security.core.Authentication auth = securityContext.getAuthentication();
                        if (auth != null && auth.isAuthenticated()) {
                            boolean isOwner = auth.getAuthorities().stream()
                                .anyMatch(a -> "ROLE_OWNER".equals(a.getAuthority()));
                            if (isOwner) {
                                org.springframework.web.context.WebApplicationContext webApplicationContext = 
                                    org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
                                es.brasatech.fastbite.application.tenant.TenantLocationService locationService = 
                                    webApplicationContext.getBean(es.brasatech.fastbite.application.tenant.TenantLocationService.class);
                                
                                if (locationService.isOwnerOf(auth.getName(), tenantId)) {
                                    java.util.List<org.springframework.security.core.GrantedAuthority> updatedAuthorities = new java.util.ArrayList<>(auth.getAuthorities());
                                    updatedAuthorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));
                                    
                                    org.springframework.security.authentication.UsernamePasswordAuthenticationToken ssoAuth = 
                                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                            auth.getPrincipal(), 
                                            auth.getCredentials(), 
                                            updatedAuthorities
                                        );
                                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(ssoAuth);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore context errors during startup or tests
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
