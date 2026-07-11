package es.brasatech.fastbite.config;

import es.brasatech.fastbite.domain.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tenantId = null;

        // Try extracting from URI template variables first (e.g. if mapped with /t/{tenantId}/**)
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables != null && pathVariables.containsKey("tenantId")) {
            tenantId = pathVariables.get("tenantId");
        }

        // Fallback: manually parse URI path starting with /t/
        if (tenantId == null) {
            String uri = request.getRequestURI();
            String contextPath = request.getContextPath();
            String path = uri.substring(contextPath.length());
            if (path.startsWith("/t/")) {
                String[] segments = path.split("/");
                if (segments.length > 2) {
                    tenantId = segments[2];
                }
            }
        }

        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
        } else {
            // Default or empty for non-tenant requests (e.g. signup)
            TenantContext.clear();
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContext.clear();
    }
}
