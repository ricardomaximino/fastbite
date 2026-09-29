package es.brasatech.fastbite.config;

import es.brasatech.fastbite.domain.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Resolves the tenant once per request and exposes it to the rest of the request
 * (persistence through {@link TenantContext}, security and views through request attributes).
 */
public class TenantContextFilter extends OncePerRequestFilter {

    private final TenantRoutingResolver tenantResolver;

    public TenantContextFilter(TenantRoutingResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        TenantRoutingResolver.ResolvedTenant tenant = tenantResolver.resolve(request);
        if (tenant != null) {
            TenantContext.setCurrentTenant(tenant.id());
            request.setAttribute("tenantId", tenant.id());
            request.setAttribute(TenantRoutingResolver.TENANT_URL_PREFIX, tenant.urlPrefix());
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
