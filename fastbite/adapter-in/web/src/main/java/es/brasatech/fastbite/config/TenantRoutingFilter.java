package es.brasatech.fastbite.config;

import es.brasatech.fastbite.domain.tenant.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class TenantRoutingFilter implements Filter {

    public static boolean isReserved(String segment) {
        if (segment == null || segment.isEmpty()) {
            return true;
        }
        return segment.equals("signup") ||
               segment.equals("login") ||
               segment.equals("css") ||
               segment.equals("js") ||
               segment.equals("images") ||
               segment.equals("webjars") ||
               segment.equals("stripe") ||
               segment.equals("error") ||
               segment.equals("favicon.ico") ||
               segment.equals("actuator") ||
               segment.equals("api") ||
               segment.equals("counter") ||
               segment.equals("backoffice") ||
               segment.equals("dashboard") ||
               segment.equals("logout") ||
               segment.equals("menu") ||
               segment.equals("select-payment") ||
               segment.equals("order-confirmation");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());

        String[] segments = path.split("/");
        if (segments.length > 1) {
            String firstSegment = segments[1];
            if (!isReserved(firstSegment)) {
                System.out.println("TenantRoutingFilter matched tenant path: " + path);
                String tenantId = firstSegment;
                System.out.println("TenantRoutingFilter extracted tenantId: " + tenantId);
                TenantContext.setCurrentTenant(tenantId);
                request.setAttribute("tenantId", tenantId);
                
                // Reconstruct the internal URI to forward to
                // e.g. /ricardomaximino/menu -> /menu
                StringBuilder internalPath = new StringBuilder();
                for (int i = 2; i < segments.length; i++) {
                    internalPath.append("/").append(segments[i]);
                }
                if (internalPath.length() == 0) {
                    internalPath.append("/");
                }
                
                String queryString = httpRequest.getQueryString();
                String forwardUri = internalPath.toString() + (queryString != null ? "?" + queryString : "");
                System.out.println("TenantRoutingFilter forwarding to: " + forwardUri);
                
                try {
                    request.getRequestDispatcher(forwardUri).forward(request, response);
                } finally {
                    TenantContext.clear();
                }
                return;
            }
        }

        // Direct/non-tenant route protection
        boolean isTenantForwarded = request.getAttribute("tenantId") != null;
        if (!isTenantForwarded) {
            if (path.equals("/menu") || path.equals("/") || path.startsWith("/select-payment") || 
                path.startsWith("/order-confirmation") || path.startsWith("/backoffice") || 
                path.startsWith("/counter") || path.startsWith("/dashboard")) {
                if (path.equals("/")) {
                    ((jakarta.servlet.http.HttpServletResponse) response).sendRedirect(contextPath + "/signup");
                    return;
                }
                ((jakarta.servlet.http.HttpServletResponse) response).sendError(404, "Tenant context required");
                return;
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
