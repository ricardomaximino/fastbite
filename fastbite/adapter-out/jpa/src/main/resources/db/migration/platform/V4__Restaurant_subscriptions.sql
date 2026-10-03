-- Existing locations receive a fresh 30-day transition trial, never an unverified paid subscription.
CREATE TABLE public.tenant_billing (
    tenant_id VARCHAR(255) PRIMARY KEY,
    billing_key VARCHAR(36) NOT NULL UNIQUE,
    trial_started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'LOCAL_TRIAL',
    customer_id VARCHAR(255) UNIQUE,
    subscription_id VARCHAR(255) UNIQUE,
    period_end BIGINT NOT NULL DEFAULT 0,
    stripe_trial_end BIGINT NOT NULL DEFAULT 0,
    cancel_at_period_end BOOLEAN NOT NULL DEFAULT FALSE,
    checkout_id VARCHAR(255) UNIQUE,
    checkout_url VARCHAR(2048),
    checkout_expires BIGINT NOT NULL DEFAULT 0,
    checkout_attempt INT NOT NULL DEFAULT 0
);
INSERT INTO public.tenant_billing (tenant_id, billing_key) SELECT tenant_id, id FROM public.tenant_locations;
UPDATE public.tenant_locations SET plan = 'RESTAURANT';
CREATE TABLE public.group_quote_requests (
    owner_username VARCHAR(100) PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    locations INT NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
