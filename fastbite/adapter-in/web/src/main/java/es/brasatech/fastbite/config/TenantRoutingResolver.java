package es.brasatech.fastbite.config;

import es.brasatech.fastbite.application.tenant.TenantLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantRoutingResolver {

    private final TenantLocationService tenantLocationService;
    private final Map<String, String> domainCache = new ConcurrentHashMap<>();

    public String resolveTenantId(String host) {
        if (host == null) {
            return null;
        }
        String cleanHost = host.split(":")[0].toLowerCase();
        
        // 1. IP Addresses check
        if (cleanHost.matches("^[0-9\\.]+$")) {
            return null;
        }

        // 2. Platform domains
        if (cleanHost.endsWith(".localhost") || cleanHost.endsWith(".fastbite.com")) {
            String[] parts = cleanHost.split("\\.");
            if (parts.length > 1) {
                String subdomain = parts[0];
                if (!"www".equals(subdomain) && !"api".equals(subdomain)) {
                    return subdomain;
                }
            }
            return null;
        }

        // 3. Custom Domain - Cache check first
        if (domainCache.containsKey(cleanHost)) {
            String cached = domainCache.get(cleanHost);
            return cached.isEmpty() ? null : cached;
        }

        // 4. Custom Domain - Database lookup fallback
        try {
            log.info("Resolving custom domain from database: {}", cleanHost);
            String tenantId = tenantLocationService.getLocationByCustomDomain(cleanHost)
                    .map(loc -> loc.tenantId())
                    .orElse(null);
            
            // Cache the result (using empty string to represent null/no tenant)
            domainCache.put(cleanHost, tenantId != null ? tenantId : "");
            return tenantId;
        } catch (Exception e) {
            log.error("Failed to resolve tenant by custom domain: " + cleanHost, e);
            return null;
        }
    }

    public void evictCache(String customDomain) {
        if (customDomain != null) {
            domainCache.remove(customDomain.trim().toLowerCase());
            log.info("Evicted domain cache for: {}", customDomain);
        }
    }
}
