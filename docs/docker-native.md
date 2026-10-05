# Native Docker build on Windows

Start Docker Desktop in **Linux containers** mode. From the repository root in PowerShell:

```powershell
docker compose build fastbite
docker compose up -d
```

Maven and GraalVM run inside the build container, so no Java, Maven or native compiler is needed on Windows. The result is a Linux native executable in a non-root runtime image. The first build downloads dependencies and native compilation can take several minutes and substantial memory. Subsequent builds reuse a Maven cache. Tests are skipped during image builds; run the test suite separately before release.

- FastBite: http://localhost:8080
- Mailpit inbox: http://localhost:8025 (SMTP on localhost:1025)
- PostgreSQL: localhost:5433 (local database/user/password: `brasatech`)

Compose configures database and SMTP connections, waits for PostgreSQL to be healthy and applies migrations automatically. PostgreSQL data persists in `postgres-data`; uploaded files persist in `fastbite-storage`. Register an owner and use the console onboarding to load a demo template; this stack does not seed demo accounts.

Choose an available restaurant address such as `kebabcafe`. `kebab` remains reserved for legacy demo routing even without seeded data; it cannot be registered as a new tenant.

If other applications use these ports, override them before starting:

```powershell
$env:FASTBITE_PORT = "18081"
$env:FASTBITE_DB_PORT = "15433"
$env:FASTBITE_SMTP_PORT = "11025"
$env:FASTBITE_MAIL_UI_PORT = "18025"
docker compose up -d
```

These settings change host ports only. The application public URL follows `FASTBITE_PORT`.

```powershell
docker compose logs -f fastbite
docker compose down
```

Stopping the stack preserves named volumes. This configuration is for local development. Production needs separate secrets, mail/payment configuration and the [migration job](database-migrations.md).

## Why the original build failed

The Dockerfile selected `-pl core`, but this repository has no Maven module named `core`. Build and install `webapplication` together with its reactor dependencies (`-am`) first, then compile only the executable module with the native profile.

The build also normalizes the wrapper's Windows line endings and executable permission. `.dockerignore` prevents host build outputs, local databases and environment files from entering the Linux build context.

The original Compose database settings were unused and did not match the PostgreSQL service. FastBite uses `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_DRIVER` and `HIBERNATE_DIALECT`. These are now attached to the application, along with SMTP, migrations and writable persistent storage.

Starting the built image also exposed Flyway's classpath scanner failing to locate embedded SQL migrations. The tenant migration service now supplies resources through Spring's native-compatible resolver and registers Java migrations explicitly. Migration validation and separate platform/restaurant histories remain enabled.
