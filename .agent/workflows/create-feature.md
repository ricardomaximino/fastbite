---
description: Create a new feature following the project's hexagonal architecture
---

# Create New Feature Workflow

This workflow provides a guided prompt and strict rules for creating new features in the FastBite project using Hexagonal Architecture.

## 🏗️ Architecture Mapping

All new classes must be created in their respective modules and packages:

| Layer | Module Path | Base Package | Responsibility |
| :--- | :--- | :--- | :--- |
| **Domain** | `domain/` | `es.brasatech.fastbite.domain.<feature>` | Entities (Records), Domain Events, Core Logic. |
| **Application** | `application/` | `es.brasatech.fastbite.application.*` | UseCases (Input Ports), Services (Orchestrators), Output Ports. |
| **Adapter In** | `adapter-in/web/` | `es.brasatech.fastbite.*` | Controllers (`.controller`), DTOs (`.dto`), Mappers. |
| **Adapter Out (JPA)** | `adapter-out/jpa/` | `es.brasatech.fastbite.jpa.<feature>` | JPA Entities, JpaRepositories, Output Port Impls. |
| **Adapter Out (Mongo)** | `adapter-out/mongodb/` | `es.brasatech.fastbite.mongodb.<feature>` | Mongo Documents, MongoRepositories, Output Port Impls. |

## 🛠️ Step-by-Step Implementation

1.  **Define Domain**: Create core domain objects (Records) in the `domain` module.
2.  **Define Application Ports**:
    - Create a `UseCase` interface in `application.usecase`.
    - Create Output Ports (Interfaces) in `application.port.out`.
3.  **Implement Application Service**: Create the service implementation in `application.service`.
4.  **Implement Adapters (Out)**:
    - **JPA Implementation**: Service in `*.jpa` package, Entity and JpaRepository.
    - **MongoDB Implementation**: Service in `*.mongodb` package, Document and MongoRepository.
5.  **Implement Adapter (In)**:
    - Create DTOs and Mappers in `adapter-in/web`.
    - Create the `@Controller` in `adapter-in/web`.
    - **UI Fragments**: Create Thymeleaf fragments in `src/main/resources/templates/fastfood/fragments/`. Use `<th:block>` as the top-level element.
    - **Data Enrichment**: Map domain objects to simple `Map<String, Object>` or DTOs in the Controller. Stringify Enums and pre-calculate totals.

## ⚠️ Critical Rules

- **Multi-Persistence Strategy**: Every service must have a JPA and a MongoDB implementation.
- **I18n Strategy**: Store in default language in main tables, keep translations separate. Translate on retrieval using the user's/order's language.
- **Server-Side Rendering**: Use Thymeleaf and fragments for all dynamic UI. Avoid generating HTML in JavaScript.
- **Data Enrichment**: Never pass complex domain entities or records directly to fragments. Always pre-process data in the Controller.
- **CSRF Protection**: All POST requests (forms and AJAX) must include CSRF tokens.

## 🚀 Shipping Checklist

- [ ] JPA Implementation verified.
- [ ] MongoDB Implementation verified.
- [ ] DTOs used for all Web inputs/outputs.
- [ ] Enums converted to Strings before rendering in fragments.
- [ ] Fragment wrappers use `<th:block>`.
- [ ] CSRF tokens included in all new forms/AJAX.
- [ ] Translations added for `default`, `es`, and `pt`.
- [ ] No inline CSS or `<style>` tags used.
- [ ] Strict equality (`===`) used in JavaScript.

---

### Command Prompt
When you use the `/create-feature` command, use this following prompt as a base:

> "I need to implement the feature: [DESCRIBE_FEATURE_HERE]. 
> 
> Please follow the hexagonal architecture rules and the Shipping Checklist defined in the project's `.agent/workflows/create-feature.md`. 
> 
> Ensure all layers are covered (Domain, Application, Adapters In/Out) for both JPA and MongoDB profiles. Use Thymeleaf fragments for any dynamic UI components and ensure data is enriched in the Controller before rendering."
