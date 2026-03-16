# GEMINI.md - FastBite Project Instructions

Fast-food ordering system with multi-persistence architecture and database-level internationalization.

---

## Tech Stack

**Backend**: Spring Boot 3.5.7, Java 25, Thymeleaf server-side rendering, Spring Mail
**Persistence**: Multi-strategy (MongoDB, JPA) via Spring profiles
**Frontend**: Bootstrap 5.3.0, Font Awesome 6.4.0, Vanilla JavaScript
**I18n**: Separate translation tables/collections with field-level fallback

---

## Architecture Overview

### Multi-Persistence Strategy

The application supports two persistence implementations:

1. **JPA Profile** (`jpa`)
   - Uses H2 (default), PostgreSQL, or MySQL
   - Separate tables for translations
   - Configuration: `application-jpa.yml`
   - Excludes: All MongoDB auto-configuration

2. **MongoDB Profile** (`mongodb`)
   - Requires MongoDB on localhost:27017
   - Separate collections for translations
   - Configuration: `application-mongodb.yml`
   - Excludes: JPA/DataSource auto-configuration

**Service Pattern**: Each service has two implementations
- `*ServiceJpaImpl` in `*.jpa` packages
- `*ServiceMongoImpl` in `*.mongodb` packages
- Shared interfaces, DTOs, and records in base packages

**Package Organization** (CRITICAL):
- ✅ JPA classes: `*.jpa` subpackages
- ✅ MongoDB classes: `*.mongodb` subpackages
- ✅ Shared code: Base packages
- ❌ NEVER mix implementations in same package

**Services**:
- BackOffice: `ProductService`, `GroupService`, `CustomizationService`
- Orders: `OrderService`

### I18n Architecture

**Core Principle**: Default language in main tables, translations in separate tables

**Storage Strategy**:
- Main entities store default language values (configured in `application.yml`)
- Translation entities store non-default languages only
- Field-level fallback: translation → default → null

**Flow**:
1. Query for entity
2. If requested locale == default locale: Return directly (fast path)
3. If requested locale != default: Load translations and merge
4. If translation field is null: Use default language value

**Translation Entities**:
- `ProductTranslation`, `GroupTranslation`, `CustomizationTranslation`
- `CustomizationOptionTranslation` (uses `optionId` for efficient lookup)

**Benefits**:
- Fast queries for default language (no joins)
- Partial translations work gracefully
- No data duplication
- Easy to add new languages

### Order I18n (Important)

Orders contain `ProductCustomizer` objects with customization option names. These must be displayed in the order's language.

**Storage Strategy**:
- Orders have `orderLanguage` field (e.g., "en", "es", "pt")
- ProductCustomizer names ALWAYS stored in default language
- Translated on retrieval based on `orderLanguage`

**Why?**
- Data consistency across all orders
- Translation updates reflect in historical orders
- Simplifies querying and reporting

**Save Process**:
```
1. Order arrives with orderLanguage = "es"
2. If orderLanguage != defaultLanguage:
   - For each ProductCustomizer
   - Look up option by ID in CustomizationOptionRepository
   - Get default language name
   - Store with default name
3. If orderLanguage == defaultLanguage:
   - Store directly (no lookup needed)
```

**Retrieve Process**:
```
1. Load order (all names in default language)
2. If orderLanguage == defaultLanguage:
   - Return as-is (fast path)
3. If orderLanguage != defaultLanguage:
   - For each ProductCustomizer
   - Look up translation in CustomizationOptionTranslationRepository
   - Replace name with translation (or keep default if not found)
```

**Implementation**:
- JPA: `OrderServiceJpaImpl` uses `CustomizationOptionJpaRepository` and `CustomizationOptionTranslationJpaRepository`
- MongoDB: `OrderServiceMongoImpl` uses `CustomizationMongoRepository` and `CustomizationOptionTranslationMongoRepository`

### Email Services

**Core Principle**: Hexagonal architecture with a shared interface in the `application` layer and implementation in the `adapter-out` layer.

**Features**:
- Text and HTML email support (Thymeleaf templates)
- Attachment support (via `MailMessageAttachment`)
- Event-driven: Publishes `MailMessageSentEvent` after successful delivery
- Locale-aware: Templates are processed using the requested language

**Flow**:
1. `EmailService.sendEmail(MailMessage)` is called
2. If template is present: HTML body is generated via `SpringTemplateEngine`
3. Email is sent via `JavaMailSender`
4. Post-send: `MailMessageSentEvent` is published for tracking/logging

**Persistence**: `EmailRepository` handles email validation and storage (implementation follows the multi-persistence strategy).

---

## Project Structure

```
es.brasatech.fastbite/
├── menu/              # Customer ordering interface
│   ├── CartItem.java, Product.java (records)
│   └── MenuController.java
├── order/             # Order management + persistence
│   ├── Order.java, OrderService.java
│   ├── OrderManager.java
│   ├── jpa/           # JPA implementation
│   │   ├── OrderEntity.java
│   │   ├── CartItemEntity.java
│   │   ├── ProductCustomizerEntity.java
│   │   ├── OrderServiceJpaImpl.java
│   │   └── JpaConfig.java
│   └── mongodb/       # MongoDB implementation
│       ├── OrderDocument.java
│       ├── OrderServiceMongoImpl.java
│       └── MongoConfig.java
└── office/            # BackOffice administration
    ├── i18n/          # I18nField, I18nConfig
    ├── product/
    │   ├── ProductDto.java, ProductI18n.java
    │   ├── jpa/       # JPA entities, repositories, service
    │   └── mongodb/   # MongoDB documents, repositories, service
    ├── group/         # Similar structure
    ├── customization/ # Similar structure
    │   ├── jpa/
    │   │   ├── CustomizationOptionEntity.java
    │   │   ├── CustomizationOptionTranslationEntity.java
    │   │   └── CustomizationOptionTranslationJpaRepository.java
    │   └── mongodb/
    │       └── CustomizationOptionTranslationMongoRepository.java
    ├── image/         # Image upload service
    └── email/         # Email adapter (adapter-out)
        └── service/   # EmailServiceImpl.java
```

**Shared Email Components**:
- `es.brasatech.fastbite.domain.mail`: `Email`, `MailMessage`, `MailMessageAttachment`
- `es.brasatech.fastbite.application.mail`: `EmailService` (interface), `EmailRepository` (interface)

---

## Critical Rules

### Code Style

1. **Keep Changes Minimal**
   - Only modify what's requested
   - No refactoring unless asked
   - No "improvements" beyond the task
   - Small, focused diffs

2. **No Documentation Files**
   - Don't create README, CHANGELOG, or docs
   - Only add comments if logic is truly non-obvious
   - Code should be self-explanatory

3. **Server-Side Focus**
   - Thymeleaf for rendering (no React, Vue, Angular)
   - Traditional form POST (no REST APIs for core features)
   - Server session for state (not localStorage/sessionStorage)

4. **Dynamic UI with Fragments**
   - Use Thymeleaf fragments for dynamic sections (modals, lists, carts)
   - JavaScript should fetch fragments via AJAX instead of generating HTML strings
   - Standardize on `th:block` as the top-level element for fragments to avoid wrapper layout issues

### CSS & Styling

- ❌ NEVER add inline CSS or `<style>` tags
- ✅ Use existing classes from `menu.css` or Bootstrap
- ❌ Don't provide CSS unless explicitly requested

### JavaScript

- ✅ Use `window.location.assign()` (not `.href`)
- ✅ Use strict equality: `===` and `!==` (never `==` or `!=`)
- ✅ Include CSRF tokens in all POST requests
- ❌ Avoid `localStorage`/`sessionStorage`

### Security

- ✅ CSRF tokens required in all forms
- ✅ Server-side validation (client-side optional)
- ✅ Store state in HTTP session

---

## Development Patterns

### Querying Entities (Automatic Locale)

```java
// Uses LocaleContextHolder.getLocale() automatically
List<ProductDto> products = productService.findAll();
Optional<ProductDto> product = productService.findById(id);
```

### Querying Entities (Explicit Locale)

```java
List<ProductDto> products = productService.findAllInLocale("es");
Optional<ProductDto> product = productService.findByIdInLocale(id, "pt");
```

### Translation Management

```java
// Get entity with all translations (for translation UI)
Optional<ProductI18n> i18n = productService.findI18nById(id);

// Update translations (saves default to main table, others to translation tables)
productService.updateI18n(id, modifiedI18n);
```

### Creating Entities

```java
// Always create in default language
ProductDto dto = new ProductDto("Burger", price, "Description", ...);
productService.create(dto);
```

### DTOs (Java Records)

```java
// Standard DTO
public record ProductDto(String name, BigDecimal price, String description,
                        String image, List<String> customizations, boolean active) {}

// I18n version (for translation UI)
public record ProductI18n(I18nField name, BigDecimal price, I18nField description,
                         String image, List<String> customizations, boolean active) {}
// I18nField contains Map<String, String> with all translations

// BackOffice wrapper
public record BackOfficeDto<T>(String id, @JsonProperty("customFields") T customFields) {}
```

### Service Methods

**Standard Methods** (use default locale from LocaleContextHolder):
- `findAll()`, `findById()`, `create()`, `update()`, `delete()`, `clear()`

**I18n Methods**:
- `findI18nById()` - Returns entity with all translations
- `updateI18n()` - Updates all translations
- `findByIdInLocale(id, locale)` - Returns DTO in specific locale
- `findAllInLocale(locale)` - Returns all DTOs in specific locale

### Email Communication

```java
// Create a basic mail message
MailMessage message = new MailMessage(
    "User Name",
    new Email("user@example.com", true),
    "Subject",
    "Text content",
    "en",
    MailMessageType.NOTIFICATION,
    MailMessageStatus.PENDING
);

// Send via service
emailService.sendEmail(message);
```

### Fragment Data Enrichment

To ensure robust fragment rendering and avoid SpEL evaluation issues with records or enums:

1. **Pre-calculate values**: Calculate totals or complex logic in the Controller.
2. **Convert Enums**: Stringify enums (`.name()`) before passing to the model for safer template comparisons.
3. **Use Simple Maps/DTOs**: Pass enriched simple objects to the view instead of complex domain entities.

```java
@GetMapping("/fragments/item-list")
public String getFragment(Model model) {
    var items = service.findAll().stream().map(item -> {
        Map<String, Object> m = new HashMap<>();
        m.put("name", item.name());
        m.put("total", item.calculateTotal()); // Pre-calculated
        m.put("status", item.status().name()); // Stringified
        return m;
    }).toList();
    model.addAttribute("items", items);
    return "fragments/file :: fragment";
}
```

---

## Thymeleaf Patterns

### Forms with CSRF

```html
<form th:action="@{/backoffice/products}" method="post">
    <input type="hidden" name="_csrf" th:value="${_csrf.token}"/>
    <input type="text" name="name" class="form-control" required>
    <button type="submit" class="btn btn-primary">Save</button>
</form>
```

### AJAX with CSRF

```javascript
const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
fetch('/api/endpoint', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': csrfToken
    },
    body: JSON.stringify(data)
})
.then(response => response.json())
.then(data => window.location.assign('/next-page'))
.catch(error => console.error('Error:', error));
```

---

## Configuration

### Switch Profiles

Edit `application.yml`:
```yaml
spring:
  profiles:
    active:
      - jpa  # or mongodb
```

### Profile-Specific Configuration

**application-jpa.yml**:
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/fast_bite.db
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
      - org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
      - org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration
```

**application-mongodb.yml**:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017
      database: fast_bite
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```

### Repository Scanning Configuration

**JpaConfig.java** (in `*.jpa` package):
```java
@Configuration
@Profile("jpa")
@EnableJpaRepositories(basePackages = {
    "es.brasatech.fastbite.order.jpa",
    "es.brasatech.fastbite.office.product.jpa",
    "es.brasatech.fastbite.office.group.jpa",
    "es.brasatech.fastbite.office.customization.jpa"
})
public class JpaConfig {}
```

**MongoConfig.java** (in `*.mongodb` package):
```java
@Configuration
@Profile("mongodb")
@EnableMongoRepositories(basePackages = {
    "es.brasatech.fastbite.order.mongodb",
    "es.brasatech.fastbite.office.product.mongodb",
    "es.brasatech.fastbite.office.group.mongodb",
    "es.brasatech.fastbite.office.customization.mongodb"
})
public class MongoConfig {}
```

---

## Order Status Flow

```
CREATED → ACCEPTED → PROCESSING → DONE → DELIVERED → COMPLETE
              ↓
          CANCELLED (any time)
```

**Dashboards**:
- Cashier: CREATED orders
- Queue: ACCEPTED orders
- Cook: PROCESSING orders
- Waiter: DONE orders
- Manager: All orders

---

## Running the Application

### Start Application

```bash
mvn spring-boot:run
```

### Start with Specific Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=jpa
mvn spring-boot:run -Dspring-boot.run.profiles=mongodb
```

### Start MongoDB (if using mongodb profile)

```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

### Endpoints

- Menu: `http://localhost:8080/menu`
- Dashboard: `http://localhost:8080/dashboard`
- BackOffice: `http://localhost:8080/backoffice`
- Translations: `http://localhost:8080/backoffice/translations/products/{id}`

---

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test

```bash
mvn test -Dtest=OrderServiceJpaImplTest
```

### Skip Integration Tests

```bash
mvn test -Dtest=!ApplicationJPATests
```

---

## Current Implementation Status

✅ **Phase 4**: Counter POS (Table management, bulk payments, order reassignment)
✅ **Persistence**: JPA and MongoDB implementations
✅ **Order Persistence**: Implemented with i18n support
✅ **Order I18n**: Customizer names translated based on orderLanguage
✅ **Profile Isolation**: Proper separation of JPA and MongoDB
✅ **WebSocket**: Real-time order status updates
✅ **Server-Side Fragments**: All dynamic Counter components migrated to fragments

---

## Important Reminders

1. **Always use profile-specific packages** (`.jpa` or `.mongodb`)
2. **Store orders with customizer names in default language**
3. **Translate customizer names on retrieval based on orderLanguage**
4. **Use fast path for default language queries**
5. **Keep diffs small and focused**
6. **No CSS unless requested**
7. **Server-side rendering only (Thymeleaf)**
9. **Use Thymeleaf fragments for dynamic UI components**
10. **Enrich data in Controllers before sending to fragments** (simplify SpEL)
11. **Native Image Build**: Register all DTOs and domain classes used in Thymeleaf templates (SpEL) or exposed via JSON in `WebAdapterHints.java` to ensure they are accessible via reflection in the native binary.


---

*Last Updated: February 2026*
