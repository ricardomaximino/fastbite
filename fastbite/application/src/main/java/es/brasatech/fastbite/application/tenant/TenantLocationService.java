package es.brasatech.fastbite.application.tenant;

import es.brasatech.fastbite.domain.tenant.TenantLocation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TenantLocationService {

    private final TenantLocationPort tenantLocationPort;

    public TenantLocationService(TenantLocationPort tenantLocationPort) {
        this.tenantLocationPort = tenantLocationPort;
    }

    public List<TenantLocation> getLocationsByOwner(String ownerUsername) {
        return tenantLocationPort.findByOwner(ownerUsername);
    }

    public Optional<TenantLocation> getLocation(String tenantId) {
        return tenantLocationPort.findByTenantId(tenantId);
    }

    public void registerLocation(String ownerUsername, String tenantId, String plan) {
        if (tenantLocationPort.findByTenantId(tenantId).isPresent()) {
            throw new IllegalArgumentException("Tenant location prefix '" + tenantId + "' is already registered.");
        }
        TenantLocation location = new TenantLocation(null, ownerUsername, tenantId, plan);
        tenantLocationPort.save(location);
    }

    public boolean isOwnerOf(String ownerUsername, String tenantId) {
        return tenantLocationPort.findByTenantId(tenantId)
                .map(loc -> loc.ownerUsername().equalsIgnoreCase(ownerUsername))
                .orElse(false);
    }

    public void removeLocation(String tenantId) {
        tenantLocationPort.delete(tenantId);
    }
}
