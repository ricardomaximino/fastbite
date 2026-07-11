package es.brasatech.fastbite.config;

import es.brasatech.fastbite.domain.tenant.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class TenantRoutingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());

        if (path.startsWith("/t/")) {
            System.out.println("TenantRoutingFilter matched path: " + path);
            String[] segments = path.split("/");
            if (segments.length > 2) {
                String tenantId = segments[2];
                System.out.println("TenantRoutingFilter extracted tenantId: " + tenantId);
                TenantContext.setCurrentTenant(tenantId);
                request.setAttribute("tenantId", tenantId);
                
                // Reconstruct the internal URI to forward to
                // e.g. /t/ricardomaximino/menu -> /menu
                StringBuilder internalPath = new StringBuilder();
                for (int i = 3; i < segments.length; i++) {
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

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
