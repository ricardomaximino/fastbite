# Restaurant backups

The back-office backup downloads a ZIP containing `data.json` and the restaurant's media. Export finishes before the HTTP download starts; serialization or file-read failures return an error instead of a partial ZIP with a successful status.

Format 2 preserves orders (including timestamps, line items, customizations, service type and payment status), table/order associations, payment configuration and denominations, catalog data, translations, staff users, restaurant settings, and the daily order counter. Timestamps are ISO-8601 strings. Platform owner accounts, location registrations, sessions, and owner password-setup tokens are outside a restaurant backup. The demo restaurant shares the platform schema, so its export, restore and cleanup explicitly preserve platform owners.

Restore targets an already-provisioned restaurant. It validates and stages the archive before replacing data. A separate transaction is opened after selecting the target restaurant, even if the caller has a transaction for another restaurant. Original media is retained until the database commit completes, and restored if that operation fails. Archives with traversal paths, duplicate media paths, unsupported versions, or more than 256 MiB of expanded content are rejected. The existing HTTP multipart upload limit still applies.

Older demo/translation archives remain readable. If settings are absent from an older archive, destination settings remain unchanged. Missing order-counter metadata is reconstructed from today's restored orders; restoration never reduces numbers already issued today in the destination restaurant.

This is a local database/filesystem restore, not a distributed transaction: a process or machine crash between the media swap and database commit can require manual recovery from the retained `.restore-*` directory. Run maintenance when the restaurant is not taking orders. PostgreSQL and native-image behavior still require deployment validation.
