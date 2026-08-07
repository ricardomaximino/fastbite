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

        // Resolve tenant from Host header (subdomain)
        String host = request.getHeader("Host");
        if (host != null) {
            String cleanHost = host.split(":")[0].toLowerCase();
            if (!cleanHost.matches("^[0-9\\.]+$")) {
                String[] parts = cleanHost.split("\\.");
                if (cleanHost.endsWith(".localhost")) {
                    if (parts.length > 1) {
                        String subdomain = parts[0];
                        if (!"www".equals(subdomain) && !"api".equals(subdomain)) {
                            tenantId = subdomain;
                        }
                    }
                } else {
                    if (parts.length > 2) {
                        String subdomain = parts[0];
                        if (!"www".equals(subdomain) && !"api".equals(subdomain)) {
                            tenantId = subdomain;
                        }
                    }
                }
            }
        }

        if (tenantId == null) {
            tenantId = request.getParameter("tenantId");
        }

        // Try extracting from request attribute first
        if (tenantId == null) {
            tenantId = (String) request.getAttribute("tenantId");
        }

        // Try extracting from URI template variables first (e.g. if mapped with /t/{tenantId}/**)
        if (tenantId == null) {
            @SuppressWarnings("unchecked")
            Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (pathVariables != null && pathVariables.containsKey("tenantId")) {
                tenantId = pathVariables.get("tenantId");
            }
        }

        // Fallback: manually parse URI path
        if (tenantId == null) {
            String uri = request.getRequestURI();
            String contextPath = request.getContextPath();
            String path = uri.substring(contextPath.length());
            String[] segments = path.split("/");
            if (segments.length > 1) {
                String firstSegment = segments[1];
                if (!isReserved(firstSegment)) {
                    tenantId = firstSegment;
                }
            }
        }

        // Fallback 2: parse Referer header for AJAX requests
        if (tenantId == null) {
            String referer = request.getHeader("Referer");
            if (referer != null) {
                try {
                    java.net.URI refererUri = new java.net.URI(referer);
                    String refererPath = refererUri.getPath();
                    if (refererPath != null && refererPath.startsWith("/")) {
                        String[] segments = refererPath.split("/");
                        if (segments.length > 1) {
                            String firstSegment = segments[1];
                            if (!isReserved(firstSegment)) {
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
        } else {
            // Default or empty for non-tenant requests (e.g. signup)
            TenantContext.clear();
        }

        return true;
    }

    public static boolean isReserved(String segment) {
        if (segment == null || segment.isEmpty()) {
            return true;
        }
        return segment.equals("signup") ||
               segment.equals("login") ||
               segment.equals("css") ||
               segment.equals("js") ||
               segment.equals("images") ||
               segment.equals("user-images") ||
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
               segment.equals("order-confirmation") ||
               segment.equals(".well-known") ||
               segment.equals("appspecific");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContext.clear();
    }
}
