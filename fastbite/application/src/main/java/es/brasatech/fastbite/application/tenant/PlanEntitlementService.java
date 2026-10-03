package es.brasatech.fastbite.application.tenant;

import es.brasatech.fastbite.domain.tenant.PlanFeature;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class PlanEntitlementService {
    // One operational plan. Trial and billing status are evaluated separately.
    private static final Set<PlanFeature> FEATURES = Set.of(PlanFeature.REVIEW_COLLECTOR, PlanFeature.KDS_TIMERS, PlanFeature.BULK_OPERATIONS, PlanFeature.ANALYTICS);
    public boolean isFeatureEnabled(String plan, PlanFeature feature) { return getEnabledFeatures(plan).contains(feature); }
    public Set<PlanFeature> getEnabledFeatures(String plan) { return "RESTAURANT".equals(plan) ? FEATURES : Set.of(); }
}
