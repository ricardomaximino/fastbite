package es.brasatech.fastbite.application.tenant;

import es.brasatech.fastbite.domain.tenant.PlanFeature;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class PlanEntitlementService {

    private final Map<String, Set<PlanFeature>> planEntitlements = new HashMap<>();

    public PlanEntitlementService() {
        // Free Demo Plan
        planEntitlements.put("Free Demo", Collections.emptySet());
        planEntitlements.put("Free", Collections.emptySet());

        // Basic Plan
        planEntitlements.put("Basic Plan", EnumSet.of(PlanFeature.REVIEW_COLLECTOR, PlanFeature.KDS_TIMERS));
        planEntitlements.put("Basic", EnumSet.of(PlanFeature.REVIEW_COLLECTOR, PlanFeature.KDS_TIMERS));

        // Pro Plan
        planEntitlements.put("Pro Plan", EnumSet.of(
            PlanFeature.ANALYTICS,
            PlanFeature.BULK_OPERATIONS,
            PlanFeature.REVIEW_COLLECTOR,
            PlanFeature.KDS_TIMERS
        ));
        planEntitlements.put("Pro", EnumSet.of(
            PlanFeature.ANALYTICS,
            PlanFeature.BULK_OPERATIONS,
            PlanFeature.REVIEW_COLLECTOR,
            PlanFeature.KDS_TIMERS
        ));

        // Enterprise Plan
        planEntitlements.put("Enterprise Plan", EnumSet.allOf(PlanFeature.class));
        planEntitlements.put("Enterprise", EnumSet.allOf(PlanFeature.class));
    }

    public boolean isFeatureEnabled(String planName, PlanFeature feature) {
        if (planName == null) {
            return false;
        }
        Set<PlanFeature> enabledFeatures = planEntitlements.getOrDefault(planName.trim(), Collections.emptySet());
        return enabledFeatures.contains(feature);
    }

    public Set<PlanFeature> getEnabledFeatures(String planName) {
        if (planName == null) {
            return Collections.emptySet();
        }
        return planEntitlements.getOrDefault(planName.trim(), Collections.emptySet());
    }
}
