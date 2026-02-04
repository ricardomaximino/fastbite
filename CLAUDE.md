# CLAUDE.md - FastBite Project

Fast-food ordering system with multi-persistence architecture and database-level internationalization.

---

## Tech Stack

**Backend**: Spring Boot 3.5.7, Java 25, Thymeleaf server-side rendering
**Persistence**: Multi-strategy (InMemory default, MongoDB, JPA) via Spring profiles
**Frontend**: Bootstrap 5.3.0, Font Awesome 6.4.0, Vanilla JavaScript
**I18n**: Separate translation tables/collections with field-level fallback

---

## Architecture

### Multi-Persistence Strategy

Two persistence implementations per service:
- **MongoDB** (mongodb profile) - Requires MongoDB on localhost:27017, separate collections for translations
- **JPA** (jpa profile) - H2/PostgreSQL/MySQL with separate tables for translations (product_translations, etc.)

**BackOffice Services**: `ProductService`, `GroupService`, `CustomizationService`
Each has: `*ServiceMongoImpl` (in `.mongodb` package), `*ServiceJpaImpl` (in `.jpa` package)

**Order Service**: `OrderService`
Has: `OrderServiceMongoImpl` (in `.order.mongodb` package), `OrderServiceJpaImpl` (in `.order.jpa` package)

**Package Organization**: Profile-specific classes in separate packages
- JPA: `*.jpa` packages (entities, repositories, services)
- MongoDB: `*.mongodb` packages (documents, repositories, services)
- Shared: Base package (interfaces, DTOs, records, enums)

Switch profiles in `application.yml`:
```yaml
spring:
  profiles:
    active:
      - jpa  # or mongodb
```

**Profile Isolation**: Each profile excludes the other's auto-configuration
- JPA profile: Excludes MongoDB auto-configuration
- MongoDB profile: Excludes JPA/DataSource auto-configuration

**Configuration Files**:
- `application-jpa.yml`: JPA configuration with MongoDB exclusions
- `application-mongodb.yml`: MongoDB configuration with JPA exclusions
- Repository scanning: Profile-specific `@EnableJpaRepositories` / `@EnableMongoRepositories`

**Critical**: Never mix persistence implementations in the same package - always use `.jpa` or `.mongodb` subpackages.

### I18n Architecture (Separate Translation Tables/Collections)

**Storage Strategy**: Default language values stored in main entities, translations in separate tables/collections
**Default Language**: Configured via `i18n.default-language` in `application.yml` (defaults to "en")
**Fallback**: requested field translation → default language field → full default entity
**Locale Resolution**: Uses Spring's `LocaleContextHolder.getLocale()` from request context

**Architecture Benefits**:
- **Fast Path**: Default language queries bypass translation tables (single query)
- **Field-Level Fallback**: Partial translations work gracefully (field by field)
- **No Duplication**: Default language not stored in translation tables
- **Scalable**: Easy to add new languages without modifying main entities

**Storage Implementations**:
- **JPA**: Main tables (products, groups, customizations) + translation tables (product_translations, etc.)
- **MongoDB**: Main collections + translation collections with compound indexes
- **InMemory**: ConcurrentHashMaps for entities + ConcurrentHashMaps for translations

**Translation Tables/Collections**:
- `ProductTranslation` - name, description translations
- `GroupTranslation` - name, description translations
- `CustomizationTranslation` - name translations
- `CustomizationOptionTranslation` - option name translations (uses optionIndex)

**Translation UI**: Server-rendered grid (rows=fields, columns=languages)
**Critical**: NO REST APIs for translations - use traditional form POST
**Critical**: NO JavaScript for translation management - pure server-side

### Order I18n Architecture

**Challenge**: Orders contain `ProductCustomizer` objects (customization option selections). The customizer names must be displayed in the language the order was placed in.

**Storage Strategy**:
- Orders store `orderLanguage` field (e.g., "en", "es", "pt")
- ProductCustomizer names always stored in **default language** (regardless of order language)
- On retrieval, translate customizer names back to order language

**Why store in default language?**
- Ensures data consistency across all orders
- Allows translation updates to reflect in historical orders
- Enables querying/reporting without language barriers

**Save Flow**:
1. Order comes in with `orderLanguage` (e.g., "es" for Spanish)
2. If `orderLanguage == defaultLanguage`: Save directly
3. If `orderLanguage != defaultLanguage`:
   - Lookup each `ProductCustomizer.id` in `CustomizationOptionRepository`
   - Get default language name
   - Store with default language name

**Retrieve Flow**:
1. Load order from database (all names in default language)
2. If `orderLanguage == defaultLanguage`: Return as-is (fast path)
3. If `orderLanguage != defaultLanguage`:
   - Lookup translations in `CustomizationOptionTranslationRepository`
   - Merge translated names back into order
   - Fallback to default language if translation missing

**Implementation**: `OrderServiceJpaImpl`, `OrderServiceMongoImpl`
**Dependencies**: `CustomizationOptionRepository`, `CustomizationOptionTranslationRepository`

---

## Project Structure

```
es.brasatech.fastbite/
├── menu/              # Customer ordering (Phase 1) ✅
│   ├── CartItem.java, Product.java, ProductCustomizer.java (records)
│   └── MenuController.java
├── order/             # Order management + persistence (Phase 2) ✅
│   ├── Order.java, OrderService.java (interface)
│   ├── OrderManager.java (business logic)
│   ├── OrderStatus.java, OrderChannel.java (enums)
│   ├── jpa/           # JPA implementation
│   │   ├── OrderEntity.java, CartItemEntity.java
│   │   ├── ProductCustomizerEntity.java (@Embeddable)
│   │   ├── OrderJpaRepository.java
│   │   ├── OrderServiceJpaImpl.java (with i18n support)
│   │   └── JpaConfig.java (repository scanning)
│   └── mongodb/       # MongoDB implementation
│       ├── OrderDocument.java, CartItemDocument.java
│       ├── ProductCustomizerDocument.java
│       ├── OrderMongoRepository.java
│       ├── OrderServiceMongoImpl.java (with i18n support)
│       └── MongoConfig.java (repository scanning)
└── office/            # BackOffice + i18n (Phase 3) ✅
    ├── i18n/          # I18nField, I18nConfig
    ├── product/       # Product management
    │   ├── ProductDto.java, ProductI18n.java
    │   ├── ProductService.java (interface)
    │   ├── jpa/       # ProductEntity, ProductServiceJpaImpl, etc.
    │   └── mongodb/   # ProductDocument, ProductServiceMongoImpl, etc.
    ├── group/         # Similar structure
    ├── customization/ # Similar structure
    │   ├── jpa/
    │   │   ├── CustomizationOptionEntity.java
    │   │   ├── CustomizationOptionTranslationEntity.java
    │   │   └── CustomizationOptionTranslationJpaRepository.java
    │   └── mongodb/
    │       ├── CustomizationOptionTranslationDocument.java
    │       └── CustomizationOptionTranslationMongoRepository.java
    └── image/         # Image upload service
```

**Templates**: `src/main/resources/templates/fastfood/`
**Static**: `src/main/resources/static/css/menu.css`, `static/js/*.js`
**I18n UI**: `templates/fastfood/translations.html`
**Config**: `application.yml`, `application-jpa.yml`, `application-mongodb.yml`

---

## Critical Rules

### CSS
- **Never** include inline CSS or `<style>` tags
- Use existing classes from `menu.css` or Bootstrap utilities
- User has already externalized CSS - do not provide CSS unless explicitly requested

### JavaScript
- Use `window.location.assign()` not `.href` (SonarQube)
- Use `===` and `!==` (never `==` or `!=`)
- Include CSRF tokens in POST requests
- **Avoid localStorage/sessionStorage** - use server session

### Server-Side Rendering
- **Thymeleaf only** - no client-side frameworks
- Traditional form POST - no REST APIs for core features
- Translation management: server-rendered forms, not JavaScript
- Store state in HTTP session, not client-side

### I18n Patterns

**Creating entities** (default language only):
```java
ProductDto dto = new ProductDto("Burger", price, "Description", ...);
productService.create(dto);  // Stores in main table with default language
```

**Querying** (automatic locale resolution):
```java
// Uses LocaleContextHolder.getLocale() automatically
List<BackOfficeDto<ProductDto>> products = productService.findAll();  // Fast path if default language
Optional<BackOfficeDto<ProductDto>> product = productService.findById(id);  // Field-level fallback if needed
```

**Translation management** (I18nField for UI):
```java
// Get entity with all translations (for translation UI)
Optional<BackOfficeDto<ProductI18n>> i18n = productService.findI18nById(id);

// Update translations (saves default to main table, others to translation tables)
productService.updateI18n(id, modifiedI18n);
```

**Explicit locale queries**:
```java
// Override LocaleContextHolder for specific language
List<BackOfficeDto<ProductDto>> products = productService.findAllInLocale("es");
Optional<BackOfficeDto<ProductDto>> product = productService.findByIdInLocale(id, "pt");
```

**How it works internally**:
1. **Fast Path**: If requested language == default language, return entity directly (no translation lookup)
2. **Translation Path**: Load entity + translation, merge with field-level fallback
3. **Fallback**: If translation field is null, use default language field value
4. **Update**: Standard `update()` modifies default language only, `updateI18n()` manages all translations

### Data Models

**DTOs**: Use Java Records (immutable)
```java
public record ProductDto(String name, BigDecimal price, String description,
                        String image, List<String> customizations, boolean active) {}
```

**I18n versions**: Used for translation UI (contains I18nField with all languages)
```java
public record ProductI18n(I18nField name, BigDecimal price, I18nField description,
                         String image, List<String> customizations, boolean active) {}
// I18nField contains Map<String, String> of all translations
```

**BackOffice wrapper**:
```java
public record BackOfficeDto<T>(String id, @JsonProperty("customFields") T customFields) {}
```

### Service Layer Patterns

**Standard methods** (default locale):
- `findAll()`, `findById()`, `create()`, `update()`, `delete()`, `clear()`

**I18n methods** (full translations):
- `findI18nById()` - Returns `*I18n` with all translations
- `updateI18n()` - Updates full translation map
- `findByIdInLocale()` - Returns DTO in specific locale
- `findAllInLocale()` - Returns all DTOs in specific locale

**Internal implementation**:
- InMemory: `Map<String, *I18n>` storage, converts to DTO on request
- MongoDB: Stores `*Document` with I18nField, requires custom converters
- JPA: Stores `*Entity` with I18nField as JSONB, uses `@JdbcTypeCode(SqlTypes.JSON)`

---

## Constraints

### Performance
- **Default locale queries** must be optimized (most common case)
- Avoid unnecessary i18n queries when default locale suffices
- Use `findAll()` not `findAllInLocale()` for default language views

### Code Changes
- Keep diffs small - change only what's requested
- No refactoring unless explicitly asked
- No "improvements" beyond the task
- No documentation files unless requested
- No comments unless logic is non-obvious

### Security
- CSRF tokens required in all forms and POST requests
- Validate input server-side (client validation optional)
- Use server session for state, not client storage

---

## Order Status Flow

```
CREATED → ACCEPTED → PROCESSING → DONE → DELIVERED → COMPLETE
              ↓
          CANCELLED (any time)
```

Dashboards: Cashier (CREATED), Queue (ACCEPTED), Cook (PROCESSING), Waiter (DONE), Manager (all).

---

## Common Patterns

### Thymeleaf Forms
```html
<form th:action="@{/backoffice/groups}" method="post">
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

### MongoDB Custom Converter (if needed)
```java
@WritingConverter
public class CustomTypeToDocumentConverter implements Converter<CustomType, Document> {
    @Override
    public Document convert(CustomType source) {
        Document doc = new Document();
        // Convert fields
        return doc;
    }
}

@ReadingConverter
public class DocumentToCustomTypeConverter implements Converter<Document, CustomType> {
    @Override
    public CustomType convert(Document source) {
        // Convert fields
        return new CustomType(...);
    }
}

// Register in MongoConfig
@Bean
public MongoCustomConversions customConversions() {
    return new MongoCustomConversions(List.of(
        new CustomTypeToDocumentConverter(),
        new DocumentToCustomTypeConverter()
    ));
}
```

---

## Translation UI Pattern

Grid layout: rows = translatable fields, columns = supported locales
Default locale highlighted in green
Add language: dropdown with locale codes
Change default: dropdown (only if all fields populated)

**Server-side only** - no JavaScript REST calls for translation management.

---

## Development

### Run Application
```bash
mvn spring-boot:run
```

### Switch Persistence
Edit `application.yml`:
```yaml
spring:
  profiles:
    active:
      - inmemory  # or mongodb, or jpa
```

### MongoDB (if using mongodb profile)
```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

### Endpoints
- Menu: `http://localhost:8080/menu`
- Dashboard: `http://localhost:8080/dashboard`
- BackOffice: `http://localhost:8080/backoffice`
- Translations: `http://localhost:8080/backoffice/translations/groups/{id}`

---

## Current State

**Phases 1-3**: Complete ✅
**Persistence**: Multi-strategy implemented (MongoDB, JPA) ✅
**I18n**: Database-level translations with UI ✅
**Order Persistence**: Implemented with i18n support (stores in default language) ✅
**Order I18n**: Translates customizer names based on orderLanguage field ✅
**WebSocket**: Order status updates ✅
**Profile Isolation**: JPA and MongoDB profiles properly separated ✅

**Focus**: Maintain established patterns, optimize for default locale, avoid over-engineering.

---

*Last Updated: January 2026*
