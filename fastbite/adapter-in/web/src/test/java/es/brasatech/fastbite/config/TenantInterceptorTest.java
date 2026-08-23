package es.brasatech.fastbite.config;

import es.brasatech.fastbite.domain.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TenantInterceptorTest {

    private TenantInterceptor interceptor;
    private TenantRoutingResolver tenantResolver;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        tenantResolver = org.mockito.Mockito.mock(TenantRoutingResolver.class);
        org.mockito.Mockito.when(tenantResolver.resolveTenantId(org.mockito.Mockito.any()))
            .thenAnswer(invocation -> {
                String host = invocation.getArgument(0);
                if (host == null) return null;
                String cleanHost = host.split(":")[0].toLowerCase();
                if (cleanHost.endsWith(".localhost")) {
                    String[] parts = cleanHost.split("\\.");
                    if (parts.length > 1) {
                        return parts[0];
                    }
                }
                return null;
            });
            
        interceptor = new TenantInterceptor(tenantResolver);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testExtractTenantFromPathVariable() throws Exception {
        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("tenantId", "tenant-path-val");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

        boolean result = interceptor.preHandle(request, response, new Object());

        assert result;
        assertEquals("tenant-path-val", TenantContext.getCurrentTenant());
    }

    @Test
    void testExtractTenantFromURI() throws Exception {
        request.setRequestURI("/tenant-uri-val/menu");
        request.setContextPath("");

        boolean result = interceptor.preHandle(request, response, new Object());

        assert result;
        assertEquals("tenant-uri-val", TenantContext.getCurrentTenant());
    }

    @Test
    void testExtractTenantFromURIWithContextPath() throws Exception {
        request.setRequestURI("/app/tenant-context-val/menu");
        request.setContextPath("/app");

        boolean result = interceptor.preHandle(request, response, new Object());

        assert result;
        assertEquals("tenant-context-val", TenantContext.getCurrentTenant());
    }

    @Test
    void testNoTenantOnNonTenantPath() throws Exception {
        request.setRequestURI("/menu");
        request.setContextPath("");

        boolean result = interceptor.preHandle(request, response, new Object());

        assert result;
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void testClearAfterCompletion() throws Exception {
        TenantContext.setCurrentTenant("some-tenant");
        interceptor.afterCompletion(request, response, new Object(), null);
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void testExtractTenantFromReferer() throws Exception {
        request.setRequestURI("/api/calculate-cart");
        request.setContextPath("");
        request.addHeader("Referer", "http://localhost:8080/tenant-ref-val/menu");

        boolean result = interceptor.preHandle(request, response, new Object());

        assert result;
        assertEquals("tenant-ref-val", TenantContext.getCurrentTenant());
    }
}
