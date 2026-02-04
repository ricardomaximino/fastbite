# Persistence Configuration Guide

This document explains how to configure and use different persistence strategies for the FastBite BackOffice system.

## Available Persistence Strategies

The application supports three different persistence strategies through Spring profiles:

1. **InMemory** (Default) - ConcurrentHashMap storage
2. **MongoDB** - Document-based NoSQL database
3. **JPA** - Relational database (PostgreSQL/MySQL)

## Architecture Overview

### Service Interfaces
- `ProductService` - Manages products
- `GroupService` - Manages menu groups/categories
- `CustomizationService` - Manages customization options

### Implementations

Each service has three implementations:

| Strategy | Product | Group | Customization |
|----------|---------|-------|---------------|
| InMemory | `ProductServiceInMemoryImpl` | `GroupServiceInMemoryImpl` | `CustomizationServiceInMemoryImpl` |
| MongoDB | `ProductServiceMongoImpl` | `GroupServiceMongoImpl` | `CustomizationServiceMongoImpl` |
| JPA | `ProductServiceJpaImpl` | `GroupServiceJpaImpl` | `CustomizationServiceJpaImpl` |

## Profile Configuration

### 1. InMemory (Default)

**Use Case**: Development, testing, quick prototyping

**Advantages**:
- No external database required
- Fast startup
- No configuration needed
- Perfect for development and testing

**Disadvantages**:
- Data is lost when application restarts
- Not suitable for production
- Limited scalability

**Configuration**:
```yaml
spring:
  profiles:
    active:
      - standalone
      - inmemory  # Default profile
```

**Run Application**:
```bash
./mvnw spring-boot:run
```

---

### 2. MongoDB

**Use Case**: Production environments requiring flexible schema, document storage

**Advantages**:
- Flexible schema (perfect for evolving data models)
- Horizontal scalability
- JSON-like document storage
- Good for complex nested data

**Disadvantages**:
- Requires MongoDB server
- More complex deployment

**Prerequisites**:
- MongoDB server running (locally or remote)

**Configuration**:

Edit `application.yml`:
```yaml
spring:
  profiles:
    active:
      - standalone
      - mongodb  # Change to mongodb
```

Or use environment variables:
```bash
export MONGO_URI=mongodb://username:password@localhost:27017/?authSource=admin
export MONGO_DATABASE=flex
```

**Run Application**:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mongodb
```

Or with Docker:
```bash
# Start MongoDB
docker run -d --name mongodb \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=password \
  mongo:latest

# Run application
export MONGO_URI=mongodb://root:password@localhost:27017/?authSource=admin
./mvnw spring-boot:run -Dspring-boot.run.profiles=mongodb
```

---

### 3. JPA (PostgreSQL/MySQL)

**Use Case**: Production environments requiring ACID compliance, relational data integrity

**Advantages**:
- ACID compliance
- Strong data integrity
- Mature ecosystem
- SQL query support
- Transaction support

**Disadvantages**:
- Fixed schema
- Requires migration for schema changes
- Vertical scaling limitations

**Prerequisites**:
- PostgreSQL or MySQL server running

#### PostgreSQL Configuration

**Environment Variables**:
```bash
export DB_URL=jdbc:postgresql://localhost:5432/flex
export DB_USER=postgres
export DB_PASSWORD=password
export DB_DRIVER=org.postgresql.Driver
export HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
export DDL_AUTO=update
```

**Run Application**:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=jpa
```

**Docker PostgreSQL**:
```bash
docker run -d --name postgres \
  -p 5432:5432 \
  -e POSTGRES_DB=flex \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  postgres:latest

export DB_URL=jdbc:postgresql://localhost:5432/flex
./mvnw spring-boot:run -Dspring-boot.run.profiles=jpa
```

#### MySQL Configuration

**Environment Variables**:
```bash
export DB_URL=jdbc:mysql://localhost:3306/flex
export DB_USER=root
export DB_PASSWORD=password
export DB_DRIVER=com.mysql.cj.jdbc.Driver
export HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect
export DDL_AUTO=update
```

**Docker MySQL**:
```bash
docker run -d --name mysql \
  -p 3306:3306 \
  -e MYSQL_DATABASE=flex \
  -e MYSQL_ROOT_PASSWORD=password \
  mysql:latest

export DB_URL=jdbc:mysql://localhost:3306/flex
./mvnw spring-boot:run -Dspring-boot.run.profiles=jpa
```

---

## Switching Between Profiles

### Method 1: Edit application.yml

```yaml
spring:
  profiles:
    active:
      - standalone
      - inmemory    # Change to: mongodb or jpa
```

### Method 2: Command Line

```bash
# InMemory
./mvnw spring-boot:run

# MongoDB
./mvnw spring-boot:run -Dspring-boot.run.profiles=mongodb

# JPA
./mvnw spring-boot:run -Dspring-boot.run.profiles=jpa
```

### Method 3: Environment Variable

```bash
export SPRING_PROFILES_ACTIVE=standalone,mongodb
./mvnw spring-boot:run
```

---

## Data Migration

### From InMemory to MongoDB

InMemory data is not persisted. You'll need to recreate data through the BackOffice UI.

### From InMemory to JPA

InMemory data is not persisted. You'll need to recreate data through the BackOffice UI.

### From MongoDB to JPA (or vice versa)

1. Export data from source database
2. Transform data format if needed
3. Import to target database

**MongoDB Export Example**:
```bash
mongoexport --db=flex --collection=products --out=products.json
```

**PostgreSQL Import Example**:
```sql
-- Create tables (Hibernate will do this with ddl-auto=update)
-- Import data using custom migration script
```

---

## Database Schema

### JPA Entities

**Products Table**:
```sql
CREATE TABLE products (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    description VARCHAR(1000),
    image VARCHAR(255),
    active BOOLEAN NOT NULL
);

CREATE TABLE product_customizations (
    product_id VARCHAR(255) NOT NULL,
    customization_id VARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

**Groups Table**:
```sql
CREATE TABLE groups (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    icon VARCHAR(255)
);

CREATE TABLE group_products (
    group_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255),
    FOREIGN KEY (group_id) REFERENCES groups(id)
);
```

**Customizations Table**:
```sql
CREATE TABLE customizations (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    options JSONB,
    usage_count INTEGER NOT NULL
);
```

### MongoDB Collections

- `products` - Product documents
- `groups` - Group documents
- `customizations` - Customization documents

---

## Troubleshooting

### MongoDB Connection Issues

```bash
# Check if MongoDB is running
docker ps | grep mongo

# Check MongoDB logs
docker logs mongodb

# Test connection
mongosh "mongodb://root:password@localhost:27017/?authSource=admin"
```

### JPA Connection Issues

```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check PostgreSQL logs
docker logs postgres

# Test connection
psql -h localhost -U postgres -d flex
```

### Profile Not Loading

Check application logs for:
```
The following 1 profile is active: "inmemory"
```

If wrong profile is active, verify:
1. `application.yml` active profiles
2. Environment variables
3. Command line arguments

---

## Production Recommendations

### For Small to Medium Applications
- **MongoDB**: Better for rapid development and flexible schemas
- **PostgreSQL**: Better for complex queries and data integrity

### For Large-Scale Applications
- **MongoDB**: Horizontal scaling, sharding
- **PostgreSQL**: Vertical scaling, read replicas

### Hybrid Approach
You can use different profiles for different environments:
- **Development**: InMemory
- **Staging**: MongoDB or PostgreSQL
- **Production**: MongoDB or PostgreSQL with replication

---

## Performance Considerations

### InMemory
- **Read**: O(1) - Instant
- **Write**: O(1) - Instant
- **Storage**: RAM only

### MongoDB
- **Read**: O(1) to O(log n) - Very fast with indexes
- **Write**: O(log n) - Fast
- **Storage**: Disk + RAM cache

### JPA
- **Read**: O(1) to O(log n) - Fast with indexes
- **Write**: O(log n) - Fast with proper indexing
- **Storage**: Disk + RAM cache

---

## Security Considerations

### MongoDB
- Enable authentication
- Use SSL/TLS connections
- Restrict network access
- Regular backups

### JPA
- Use connection pooling (HikariCP)
- Enable SSL connections
- Use prepared statements (default in JPA)
- Regular backups
- Database user with minimal privileges

---

## Monitoring

### InMemory
- JVM memory monitoring
- No database metrics

### MongoDB
- MongoDB Atlas monitoring (if using Atlas)
- Custom metrics via Spring Boot Actuator
- MongoDB Compass for manual inspection

### JPA
- Connection pool metrics (HikariCP)
- Hibernate statistics
- Database-specific monitoring tools
- Spring Boot Actuator endpoints

---

## Version History
- **v1.0** - Initial multi-persistence architecture
- Profile-based service selection
- Support for InMemory, MongoDB, and JPA

*Last Updated: January 2026*
