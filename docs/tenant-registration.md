# Retry-safe restaurant registration

`public.tenant_lifecycle` reserves a normalized restaurant identifier for one operation and owner. Signup uses `signup:<tenant>`, an owner-console addition uses `location:<tenant>`, and Stripe uses its checkout session ID (not the event ID). Different events for the same checkout therefore share an operation. Conflicting owners or checkouts cannot overwrite a reservation.

The registration states are:

| State | Meaning |
| --- | --- |
| PROVISIONING | Reserved; setup is running or was interrupted. |
| FAILED | A setup attempt failed; the same operation can retry. |
| AWAITING_OWNER | Schema, disabled owner accounts and location exist; the setup email was sent. |
| ACTIVE | Registration completed, including password setup for an invited owner. |

These states describe registration, not subscription billing, restaurant opening hours, or access suspension. Existing locations without a lifecycle entry continue to work and remain protected against new registrations. Existing paid invitations are backfilled by the platform migration: completed invitations become ACTIVE; pending invitations remain retryable. Platform lifecycle records are not part of restaurant backup/restore or clean operations.

Registration holds a database row lock on a dedicated connection. Competing workers use NOWAIT and return a retryable failure rather than consuming the connection pool waiting for one another. Stripe receives HTTP 500 and may retry. A browser request displays an error and can be resubmitted. A completed duplicate is a no-op. At least two database connections must be available for one registration; schema creation, persistence and email delivery run synchronously.

Schema DDL may leave an empty or partial schema after failure. The reservation remains and the same operation retries schema migration. Failed Flyway history requires operator recovery before registration can resume; see database-migrations.md. A new owner is inserted in PUBLIC and the restaurant schema together with the location in one JDBC transaction. Neither account can be partially committed or overwritten. A crash after that transaction but before the lifecycle update is recovered from the matching location record without changing credentials. Tenant context is never changed by registration, and Flyway manages the provisioning connection and schema selection.

For invited owners, a failed email send remains retryable and replaces the token. Once sending succeeds, duplicates retain the valid link and send no further email. Replaying the original checkout after link expiry issues a replacement. Password completion updates both owner accounts and lifecycle state in the same transaction. SMTP and database commit cannot be atomic: a crash immediately after delivery may still cause a replacement email on retry.

No automatic schema deletion, refund, suspension, or background retry is introduced. An operator should investigate persistent FAILED reservations, ownership conflicts on a paid checkout, and partially populated legacy schemas; never delete a reservation merely to permit a different owner to reuse the name. Removing a location mapping also does not release its reservation.

Tests cover duplicate requests, competing app instances, partial DDL, account-write rollback, interrupted completion, tenant-context preservation, owner/checkout conflicts, invalid identifiers, failed email delivery, link expiry and activation. H2 is verified locally; PostgreSQL/Neon and native-image validation remain separate work.
