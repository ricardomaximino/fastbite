# FastBite - Developer Onboarding Guide

FastBite is a high-performance fast-food ordering system featuring a multi-persistence architecture (JPA/H2/PostgreSQL and MongoDB) and database-level internationalization. It is fully optimized for GraalVM Native Image deployments.

---

## 📋 Prerequisites

Before setting up and running FastBite locally, ensure your development environment has the following installed:

*   **Java 25**: FastBite uses modern Java 25 features. A GraalVM JDK 25 (e.g., GraalVM Community Edition) is highly recommended, especially if you plan to compile native images.
*   **Maven 3.9+**: For dependency management and builds.
*   **Docker & Docker Compose**: Needed to run external services like PostgreSQL/pgvector and RabbitMQ.

---

## 📥 Project Import & IntelliJ IDEA Setup

FastBite is a multi-module Maven project. Follow these steps to import and run the application in IntelliJ IDEA (on both Windows and Linux):

### 1. Import the Project
1. Open IntelliJ IDEA.
2. Select **Open** and select the root directory containing the parent `pom.xml`.
3. Choose **Open as Project**. Let IntelliJ import the Maven dependencies.

### 2. Configure the JDK
Ensure the project JDK is set to Java 25:
*   Go to **File** -> **Project Structure** -> **Project** and set the **SDK** to your Java 25 installation.
*   Under **Project Structure** -> **Modules**, verify that all modules are set to use Java 25.

### 3. Running via Run/Debug Configurations
To start the application within IntelliJ, configure a Spring Boot or Application Run Configuration:

1. Click on **Run** -> **Edit Configurations...**
2. Click the **+** (Add New Configuration) and choose **Spring Boot** (or **Application** if the Spring Boot plugin is not installed).
3. Set the following details:
   *   **Name**: `FastBite [JPA - Default]` or `FastBite [MongoDB]`
   *   **Main class**: `es.brasatech.fastbite.Application`
   *   **Use classpath of module**: `webapplication`
4. Set the active profile using **VM Options** (under Modify Options if not visible):
   *   **For JPA (Default, using local H2 database)**:
       ```bash
       -Dspring.profiles.active=jpa
       ```
   *   **For MongoDB (Requires MongoDB running)**:
       ```bash
       -Dspring.profiles.active=mongodb
       ```
5. Click **Apply** and then **OK**.

---

## 🐳 Running External Services with Docker Compose

FastBite uses `docker-compose.yml` to define and launch companion services.

### Start External Services
To start PostgreSQL (with pgvector support) and RabbitMQ in the background, run:
```bash
docker compose up -d
```

*   **RabbitMQ**: Accessible at `localhost:5672`. The management console runs at `http://localhost:15672` (default credentials: `guest`/`guest`).
*   **PostgreSQL**: Accessible at `localhost:5432` (database: `fast_bite`).

### Stopping Services
To stop and remove containers, run:
```bash
docker compose down
```

---

## 🛠️ Native Image Build

FastBite is optimized for GraalVM Native Image compilation, allowing instant startup times and minimal memory usage.

To compile the application to a standalone native binary, use the `native` Maven profile:

```bash
# Clean and compile the application to native binary
mvn clean package -Pnative -pl webapplication -am
```

This compiles a native executable in the target directory of the `webapplication` module:
*   **Windows**: `webapplication/target/fastbite.exe`
*   **Linux/macOS**: `webapplication/target/fastbite`

Run the compiled native binary directly:
```bash
# Windows
.\webapplication\target\fastbite.exe --spring.profiles.active=jpa

# Linux / macOS
./webapplication/target/fastbite --spring.profiles.active=jpa
```

---

## 🧪 Verification & Local Tests

Run the test suite to verify your local environment is correctly configured:

```bash
# Run all unit and integration tests
mvn clean test
```

### Key Verification Checks:
1. **Maven Build**: Verify that all modules (`domain`, `application`, `adapter-in`, `adapter-out`, `webapplication`) compile successfully.
2. **Accessing the UI**: After starting the application, verify it is running by visiting:
   *   **Customer Menu**: `http://localhost:8080/menu`
   *   **BackOffice Management**: `http://localhost:8080/backoffice`
   *   **Cashier/Orders Dashboard**: `http://localhost:8080/dashboard`

---

## 🏷️ Phase 6: Dynamic Promos & Discounts

FastBite features a robust rule-based promotional and discount engine that supports:

*   **Discount Scopes**:
    *   `ORDER`: Applies to individual orders.
    *   `TABLE`: Gathers active orders currently bound to a table to compute eligibility thresholds and applies a proportional, scaled-down discount to each customer's order.
*   **Discount Types**:
    *   `PERCENTAGE`: Subtracts a percentage of the eligible subtotal.
    *   `FIXED_AMOUNT`: Subtracts a flat currency amount.
*   **Application Modes**:
    *   `AUTOMATIC`: Applied automatically when subtotal requirements are met (no coupon code needed).
    *   `MANUAL`: Requires the customer to enter a specific coupon code during checkout.
*   **Internationalization (i18n)**: Translation rules are supported for all discount titles/names, allowing back-office operators to input localized promotional texts for their target languages.