# Owner account setup

Use `SPRING_PROFILES_ACTIVE=jpa` in production. The `local` or `demo` profile explicitly enables both the demo catalog and demo logins. Disabling these profiles does not remove accounts already present in an existing database; audit those accounts before deployment.

Set `SMTP_PASSWORD` through deployment secrets, and rotate the previously committed SMTP password. Removing it from the current file does not remove it from Git history. Optional mail overrides are `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_AUTH`, and `SMTP_STARTTLS`.

Set `FASTBITE_PUBLIC_URL` to the public HTTPS origin of this FastBite deployment, for example `https://app.example.com`. Setup emails use this trusted origin, never a browser-supplied host. HTTP is accepted only for localhost development.

A paid Stripe platform checkout for a new owner must have `type=PLATFORM_SUBSCRIPTION`, `tenantId`, `username`, and optionally `fullName` and `plan` in its metadata. The destination email is read from the checkout customer details/email returned by Stripe. Missing email or conflicting account details fail provisioning without overwriting existing users.

The owner starts disabled in both the platform and restaurant schemas. The emailed link expires after 24 hours and works once. Passwords require at least 12 characters and must fit BCrypt's 72-byte limit. Setting the password enables both accounts in one database transaction.

After successful email delivery, duplicate Stripe events retain the same valid link and do not send another email. A failed delivery or replay after link expiry issues a replacement link to the same recipient; only the latest link works. A failed SMTP send returns an error to Stripe so it retries. After setup, repeated checkout events neither change the password nor send email. To replace an expired setup link, an operator can resend the original checkout event from Stripe. There is no public resend endpoint yet.

Additional locations for existing owners retain the existing owner account. `customer.subscription.created` alone no longer creates an owner; provisioning requires a paid checkout completion or asynchronous payment-success event.

The runnable app now includes the email adapter. Its older generic mail-message workflow remains opt-in via `legacy-mail` and requires its missing persistence adapters; owner setup does not use that workflow.

For local demo smoke tests:

```powershell
java -jar webapplication/target/webapplication-0.0.1-SNAPSHOT.jar --spring.profiles.active=jpa,local
```

`OwnerSetupFlowTest` starts the real HTTP server against an isolated H2 database and captures outgoing mail at the SMTP boundary. It verifies setup, single use, and platform/restaurant owner login without sending real email.
