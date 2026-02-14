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
    - Create Output Ports (Interfaces) in `application.port.out` (or appropriate subpackage).
3.  **Implement Application Service**: Create the service implementation in `application.service` that implements the `UseCase` and uses the Output Ports.
4.  **Implement Adapters (Out)**:
    - **JPA Implementation**: Create `@Entity` and `JpaRepository` in `adapter-out/jpa`. Implement the Output Port.
    - **MongoDB Implementation**: Create `@Document` and `MongoRepository` in `adapter-out/mongodb`. Implement the Output Port.
5.  **Implement Adapter (In)**:
    - Create DTOs and Mappers in `adapter-in/web`.
    - Create the `@Controller` in `adapter-in/web` to expose the feature.

## ⚠️ Critical Rules
- **Profile Isolation**: Maintain strict profile isolation (`jpa` vs `mongodb`) as defined in `MEMORY[GEMINI.md]`.
- **No Mixing**: NEVER mix JPA and MongoDB classes in the same package.
- **Dependency Flow**: Adapters -> Application -> Domain. Never the reverse.
- **I18n**: Follow the established `I18nField` and translation entity patterns for persistent localized fields.

---

### Command Prompt
When you use the `/create-feature` command, use this following prompt as a base:

> "I need to implement the feature: [DESCRIBE_FEATURE_HERE]. 
> 
> Please follow the hexagonal architecture rules defined in the project's `.agent/workflows/create-feature.md`. 
> 
> Ensure all necessary 'staff' are created:
> 1. Domain Entities/Records
> 2. Application UseCases (Input Ports) and Output Ports
> 3. Application Services (Implementations)
> 4. Adapter-In Controllers and DTOs
> 5. Adapter-Out JPA Entities and Repositories
> 6. Adapter-Out MongoDB Documents and Repositories
> 
> Do not skip any layer. Maintain strict module separation."
