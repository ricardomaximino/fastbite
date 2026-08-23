package es.brasatech.fastbite.jpa.tenant;

import es.brasatech.fastbite.application.tenant.TenantLocationPort;
import es.brasatech.fastbite.domain.tenant.TenantLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TenantLocationRepositoryAdapter implements TenantLocationPort {

    private final TenantLocationJpaRepository repository;

    @Override
    public List<TenantLocation> findByOwner(String ownerUsername) {
        return repository.findByOwnerUsername(ownerUsername).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<TenantLocation> findByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId)
                .map(this::toDomain);
    }

    @Override
    public Optional<TenantLocation> findByCustomDomain(String customDomain) {
        return repository.findByCustomDomain(customDomain)
                .map(this::toDomain);
    }

    @Override
    public void save(TenantLocation location) {
        TenantLocationEntity entity = repository.findByTenantId(location.tenantId())
                .orElse(new TenantLocationEntity());
        entity.setOwnerUsername(location.ownerUsername());
        entity.setTenantId(location.tenantId());
        entity.setPlan(location.plan());
        entity.setCustomDomain(location.customDomain());
        repository.save(entity);
    }

    @Override
    public void delete(String tenantId) {
        repository.findByTenantId(tenantId)
                .ifPresent(entity -> repository.deleteById(entity.getId()));
    }

    private TenantLocation toDomain(TenantLocationEntity entity) {
        return new TenantLocation(entity.getId(), entity.getOwnerUsername(), entity.getTenantId(), entity.getPlan(), entity.getCustomDomain());
    }
}
