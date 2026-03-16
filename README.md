# FastBite

Fast-food ordering system with multi-persistence architecture and database-level internationalization. Built for performance, scalability, and modern deployment (including GraalVM Native Image).

---

## 🚀 Quick Start

### Prerequisites

- **Java 25** (GraalVM Java 25 recommended for native builds)
- **Maven 3.9+**
- **Docker** (optional, for MongoDB or PostgreSQL)

### Standard Build and Run

```bash
# Full project build
mvn clean install -DskipTests

# Run with JPA (H2 default)
mvn spring-boot:run -pl webapplication -Dspring-boot.run.profiles=jpa

# Run with MongoDB
mvn spring-boot:run -pl webapplication -Dspring-boot.run.profiles=mongodb
```

---

## 🏗️ Architecture

### Multi-Persistence Strategy

FastBite supports two storage engines via Spring Profiles:

1. **JPA Profile (`jpa`)**: Uses H2 (dev) or PostgreSQL/MySQL. Includes built-in support for translation tables.
2. **MongoDB Profile (`mongodb`)**: Document-based storage with companion translation collections.

### I18n Architecture

- **Default Language**: Stored directly in main entities for high performance.
- **Translations**: Stored in separate tables/collections.
- **Fallback Mechanism**: Automatically falls back from translation -> default -> null.

---

## 🛠️ Native Image (GraalVM)

FastBite is optimized for GraalVM Native Image, providing near-instant startup and low memory footprint.

### Build Instructions

The build handles complex aspects like Hibernate bytecode enhancement and SpEL reflection hints automatically.

```bash
# Build the native binary (JPA profile)
mvn clean package -Pnative,jpa -DskipTests

# Run the native binary
./webapplication/target/fastbite-app
```

### Key Native Optimizations

- **Bytecode Enhancement**: Hibernate proxies are generated at build-time via `hibernate-enhance-maven-plugin`.
- **Reflection Hints**: Centralized in `WebAdapterHints.java` for DTOs and ThymeLeaf utilities.
- **Java 25 Support**: Includes Byte Buddy overrides (`1.18.7`) for compatibility with Java 25 class formats.

---

## 🗄️ Database Setup

### Spring Session (JPA)

If using JPA with Spring Session, ensure the following tables exist:

```sql
CREATE TABLE SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BYTEA NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);
```

---

## 🐳 Docker

### Native Image

```bash
docker build --file docker/Dockerfile --tag ricardomaximino/fastbite-native:latest .
```

### Non-Native

```bash
docker build --file docker/Dockerfile-NonNative --tag ricardomaximino/fastbite-standard:latest .
```