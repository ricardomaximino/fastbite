package es.brasatech.fastbite.domain.tenant;

public record TenantLocation(
    String id,
    String ownerUsername,
    String tenantId,
    String plan,
    String customDomain,
    String stripeAccountId
) {
    public TenantLocation(String id, String ownerUsername, String tenantId, String plan, String customDomain) {
        this(id, ownerUsername, tenantId, plan, customDomain, null);
    }
}
