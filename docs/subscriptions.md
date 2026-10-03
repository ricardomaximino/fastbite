# Restaurant subscriptions and the platform funnel

FastBite has one operational plan: `RESTAURANT`, EUR 49 per calendar month per location, excluding VAT. The price constant is `SubscriptionPlan.MONTHLY_CENTS`. Staff accounts are included. Customer-order processing fees are separate; FastBite adds no application fee to those payments. No annual plan or consolidated group invoice is implemented.

## Trial and access

Registration creates a 30-day, no-card trial atomically with the location in `public.tenant_billing`. Retrying registration, restoring a restaurant backup or changing its menu cannot restart the trial. Additional locations each get their own trial. The public home page has a sample-only interactive demo that never creates real orders or charges.

The trial is application-managed, not a Stripe trial. Activating Restaurant starts paid billing immediately, even before the free trial ends; signup, billing and the FAQ explain this. The owner must explicitly go through Stripe Checkout. There is no automatic charge at trial expiry.

The server checks billing before accepting new guest or counter orders. An unexpired local trial, or a verified Stripe `active`/`trialing` subscription within its recorded period, permits new orders. `past_due`, `unpaid`, `paused`, `incomplete`, `incomplete_expired` and `canceled` do not. Scheduled cancellation retains access until the verified period ends. Existing orders can still be finished or paid; login, owner tools and backup export remain available. No data is deleted on billing failure. The old delete-location route directs owners to billing instead of removing the ownership mapping and stranding a subscription.

The `kebab` demo bypass is restricted to the explicit `local` or `demo` profile. Do not enable those profiles in production. Billing state is independent of registration lifecycle state and is excluded from restaurant ZIP backups.

## Stripe setup before accepting subscriptions

1. Create a Restaurant Product and a fixed recurring Price: EUR 49.00, interval month, interval count 1, tax behavior exclusive. Set its ID in `STRIPE_RESTAURANT_PRICE_ID`. Checkout verifies those attributes and uses quantity 1; the browser cannot submit an amount or choose another tier.
2. Set `STRIPE_SECRET_KEY` and the trusted HTTPS origin `FASTBITE_PUBLIC_URL`. Subscription return URLs never use the request Host header.
3. Configure the Stripe customer portal for payment method updates, invoice history and cancellation at period end. Disable plan switching, quantity changes and payment-collection pauses for this launch offer. The app verifies one supported subscription item, and will reject unsupported changes. Each location has a separate Stripe customer and subscription; its portal cannot expose another owner's locations. This also means billing is currently separate per location.
4. Configure your applicable tax treatment before launch. Set `STRIPE_AUTOMATIC_TAX_ENABLED=true` only after configuring Stripe Tax and the relevant registrations. With it false, Checkout does not calculate tax automatically. Test the actual tax and invoice result before selling; the advertised price excludes VAT.
5. Add a **platform account** webhook destination at `/api/webhooks/stripe`. Set its signing secret in `STRIPE_PLATFORM_WEBHOOK_SECRET`, separately from the existing restaurant/Connect secret. Subscribe to `checkout.session.completed`, `checkout.session.async_payment_succeeded`, `customer.subscription.created`, `customer.subscription.updated`, `customer.subscription.deleted`, `customer.subscription.paused`, `customer.subscription.resumed`, `invoice.paid` and `invoice.payment_failed`.
6. Rehearse in Stripe test mode: initial checkout, duplicate clicks, renewal, failed payment and recovery, cancellation at period end, immediate cancellation and webhook retries. No live Stripe products, prices, subscriptions, portal configuration or webhooks are created by this code change.

The shared webhook endpoint verifies the raw payload signature. It re-fetches the Checkout/Subscription rather than treating a browser return or event payload as paid access. Subscription metadata must match the location, owner, billing identity and supported price. Updates are serialized per location, with the latest subscription fetched inside the database row lock; duplicates and delayed events cannot apply an old event snapshot. API failures return an error so Stripe can retry. Owners can also use **Refresh status** to reconcile their linked checkout and subscription. Access fails closed at the recorded period boundary if renewal webhooks have not arrived, so monitor webhook failures before launch.

Stripe references: [subscription events](https://docs.stripe.com/billing/subscriptions/webhooks), [webhook delivery and signatures](https://docs.stripe.com/webhooks), [portal configuration](https://docs.stripe.com/customer-management/configure-portal).

## Checkout retry safety

A checkout attempt is committed before contacting Stripe. Concurrent requests reuse it, and network retries use the same idempotency key and fixed session deadline. The server reuses an open checkout rather than creating another subscription. An active or overdue subscription must be managed through its existing portal.

An unresolved attempt has a 23-hour deadline and stops automatic creation retries during the final 31 minutes. This keeps retries inside Stripe's idempotency retention and Checkout expiration constraints. A signed completion webhook can recover a checkout whose session ID was not saved because the process crashed. If an unresolved attempt reaches its deadline, an operator must inspect Stripe using its billing identity and attempt metadata, recover the matching session/subscription or prove that none was created before clearing that pending attempt. Never clear a pending attempt just to allow another charge. [Stripe idempotency documentation](https://docs.stripe.com/api/idempotent_requests).

## Existing locations and volume quotes

Migration V4 preserves restaurant data, canonicalizes old plan labels and grants existing locations a fresh 30-day transition trial. It does **not** infer recurring payment from an old `Pro`/`Basic` label or historical one-time checkout. Communicate this transition before rollout. Historical one-time payment webhooks can still finish registration, but only receive trial access. Operators must reconcile any externally created subscriptions separately.

Owners with 3–1000 locations can request a volume quote from their console. One durable request per owner is saved in `public.group_quote_requests`; repeat submissions update the contact and count. Operators should review that queue and contact the owner. There is no automatic quote, email notification or guaranteed response time. Agree discounts manually and supply a Stripe promotion code before checkout; the base plan stays Restaurant. No consolidated group billing is advertised.

## Verification and remaining rollout work

Automated tests cover trial boundaries, access statuses, ownership checks, supported Stripe price validation, recurring Checkout parameters, durable retries, concurrent checkout creation, webhook routing, database rollback and full HTTP signup/billing/expiry/export flows. The homepage, sample preview and signup are also checked in the browser at desktop and mobile sizes.

Live Stripe checkout/renewal/tax behavior, PostgreSQL/Neon and native-image execution still require separate validation. Conversion improvement is a design goal, not a measured result: use real signup and paid-conversion data before claiming uplift or adding more tiers. The funnel contains no invented testimonials, customer counts or savings statistics.
