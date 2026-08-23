package es.brasatech.fastbite.domain.tenant;

public record TenantLocation(
    String id,
    String ownerUsername,
    String tenantId,
    String plan,
    String customDomain
) {}
