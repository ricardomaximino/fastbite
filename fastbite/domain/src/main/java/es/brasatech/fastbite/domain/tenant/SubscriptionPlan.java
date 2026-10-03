package es.brasatech.fastbite.domain.tenant;

/** Stable identifiers, independent of Stripe price IDs and billing status. */
public enum SubscriptionPlan {
    RESTAURANT;
    public static final long MONTHLY_CENTS = 4900;
    public static final int TRIAL_DAYS = 30;
}
