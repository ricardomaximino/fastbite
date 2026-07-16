package es.brasatech.fastbite.config;

import es.brasatech.fastbite.domain.tenant.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class TenantContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantId = httpRequest.getParameter("tenantId");

        // 1. Resolve tenant from URL path
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());
        String[] segments = path.split("/");
        if (segments.length > 1) {
            String firstSegment = segments[1];
            if (!TenantInterceptor.isReserved(firstSegment)) {
                tenantId = firstSegment;
            }
        }

        // 2. Fallback: Resolve tenant from Referer header (e.g. AJAX requests or /login POST)
        if (tenantId == null) {
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
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
