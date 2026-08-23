# FastBite Roadmap

This document outlines the product evolution, configuration patterns, development milestones, and deployment strategies for **FastBite**, from a single local restaurant solution to a fully multi-tenant, cloud-native SaaS platform.

---

## 1. Achieved Milestones & Completed Features

### Phase 1 to 4: Core Ordering & Architecture
* **Hexagonal Architecture Core**: Clear ports/adapters isolation separating business logic from web controllers and persistence.
* **Unified Menu, Order, & POS dashboards**: Real-time kitchen updates via WebSocket STOMP.

### Phase 5: QR & Pre-payment Integration (Completed)
* **Table QR Ordering**: Table-bound guest menu paths.
* **Stripe Webhook integration**: Pre-payment checks and auto-provisioning order states.

### Phase 6: Design & Rules Engine (Completed)
* **Visual Themes**: Dynamic styling switcher with Cafe, Modern, and FastFood templates.
* **Rule-Based Discounts**: Automatic or coupon-based percentages and fixed discounts.
* **Strict Thymeleaf 3.1+ AOT Compliance**: Replaced inline event variables with data attributes.

### Phase 7: Multi-Tenant & Multi-Location SaaS (Completed)
* **Dynamic Multi-Tenant Subdomain Routing**: Extracting tenant contexts from host prefixes.
* **Owner Console (`/owner/console`)**: A unified management console where owners can provision new branch locations, reset database schemas, seed demo templates, and manage staff.
* **Single Sign-On (SSO)**: Session sharing across subdomains using wildcard session cookies.
* **Dual Role Mapping**: Automatic assignment of both `ROLE_OWNER` and `ROLE_ADMIN` roles to SaaS Tenant Owners.
* **Refactored Core Services**: Decoupled staff account operations from the presentation layer into standard application services.
* **AOT Native Compilation**: Verified boot-up times of under 100ms using GraalVM JDK 25 native images.

---

## 2. Future SaaS Roadmap

### Phase 8: Custom Domains & Cloud Scaling (Q3 2027)
* **Automated SSL for Custom Domains**: Integrate with Let's Encrypt / Cloudflare APIs to automate SSL provisioning when owners bind custom domains (e.g., `orders.mybrand.com`).
* **Cloud Run Scale-to-Zero Optimization**: Implement serverless container tuning to optimize database connection pooling during cold starts.
* **Dynamic Stripe Billing**: Connect the location provisioning console to Stripe Subscription billing to automatically charge owners per active branch.
