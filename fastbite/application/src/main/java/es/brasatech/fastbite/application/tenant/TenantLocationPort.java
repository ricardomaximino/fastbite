package es.brasatech.fastbite.application.tenant;

import es.brasatech.fastbite.domain.tenant.TenantLocation;

import java.util.List;
import java.util.Optional;

public interface TenantLocationPort {
    List<TenantLocation> findByOwner(String ownerUsername);
    Optional<TenantLocation> findByTenantId(String tenantId);
    Optional<TenantLocation> findByCustomDomain(String customDomain);
    void save(TenantLocation location);
    void delete(String tenantId);
}
