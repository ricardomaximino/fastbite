# Abandoned online takeaway orders

An unpaid ONLINE / TAKEAWAY order still in CREATED expires one hour after its saved creation time. It becomes CANCELLED with the reason `Online payment not received within one hour`; its lines and saved price remain available for support and backups. Paid orders, counter takeaway, table orders, and orders already in preparation are unaffected.

Cleanup runs when the restaurant's order list is read, or when that specific order is read (including its payment page and payment confirmation). It uses the selected restaurant schema. Idle restaurants need no background worker, which suits scale-to-zero hosting; their orders are cleaned on the next access, not at an exact scheduled instant.

The payment page explains cancellation in English, Spanish, or Portuguese and links back to the restaurant menu. A new checkout cannot start for an expired order.

Expiry and marking an order paid lock the same database row within a transaction. If payment wins, cleanup leaves it alone. If cleanup wins, a later verified Stripe payment marks it PAID and restores CREATED so it reaches the kitchen. Duplicate confirmations are harmless. A manually cancelled order stays cancelled even if a payment arrives; staff should reconcile such payments with the customer.

Existing Stripe checkout sessions are not revoked by this local cleanup. A customer may still complete one after local expiry; payment confirmation deliberately restores that order. Cancelling remote sessions, refund automation, and preventing multiple concurrent checkout sessions are separate work.

Integration coverage includes the one-hour boundary, tenant isolation, payment/expiry races in both orders, late and duplicate payment, manual cancellation, and preserving saved prices and line identities. Database tests run on H2; PostgreSQL locking behavior and native-image execution still need validation.
