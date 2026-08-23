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

    private final TenantRoutingResolver tenantResolver;

    public TenantInterceptor(TenantRoutingResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tenantId = null;

        // 1. Resolve from URL path first to check if it's a reserved platform path
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());
        String[] segments = path.split("/");
        boolean isReservedPath = false;
        if (segments.length > 1) {
            String firstSegment = segments[1];
            if (isReserved(firstSegment)) {
                isReservedPath = true;
            } else {
                tenantId = firstSegment;
            }
        }

        // 2. Only resolve subdomain from Host if not a reserved platform path
        if (!isReservedPath) {
            if (tenantId == null) {
                String host = request.getHeader("Host");
                tenantId = tenantResolver.resolveTenantId(host);
            }
        }

        // 3. Fallbacks (parameter, attribute, path variables, referer) are resolved even for API/reserved requests
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

        // Fallback 2: parse Referer header for AJAX requests
        if (tenantId == null) {
            String referer = request.getHeader("Referer");
            if (referer != null) {
                try {
                    java.net.URI refererUri = new java.net.URI(referer);
                    String refererPath = refererUri.getPath();
                    if (refererPath != null && refererPath.startsWith("/")) {
                        String[] segmentsRef = refererPath.split("/");
                        if (segmentsRef.length > 1) {
                            String firstSegment = segmentsRef[1];
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
               segment.equals("logout") ||
               segment.equals("menu") ||
               segment.equals("select-payment") ||
               segment.equals("order-confirmation") ||
               segment.equals(".well-known") ||
               segment.equals("appspecific") ||
               segment.equals("owner");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContext.clear();
    }
}
