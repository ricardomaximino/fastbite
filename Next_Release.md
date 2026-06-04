# FastBite Roadmap - Next Release

This document outlines the priority, design patterns, and implementation checklist for the upcoming releases of the FastBite multi-tenant system.

---

## 1. Table Ordering (QR Code Flow)
*   **Goal**: Allow a customer seated at a table to scan a QR code, enter their name, place an order, and choose either immediate online payment or request the cashier to charge them at the table.
*   **Key Tasks**:
    - [ ] **Table-Aware URL Route**: Support `/{tenantId}/menu?table={tableNumber}` or a path-based routing equivalent. Save the table number in the HTTP Session.
    - [ ] **User Identity**: Prompt the guest for their name on entering the menu or before checking out (save in Session).
    - [ ] **Payment Decision on Checkout**:
        - Provide two buttons at checkout: `Pay Online (Stripe)` or `Pay at Table`.
        - If `Pay at Table` is selected: Set the `OrderPaymentStatus` to `UNPAID` and state to `CREATED`. Trigger a WebSocket alert to the Cashier/Waiter dashboard indicating *"Table X requested payment visit"*.

---

## 2. Takeaway Ordering
*   **Goal**: Allow customers to select takeaway, providing their name and contact info (phone/email), only if takeaway is enabled by the tenant.
*   **Key Tasks**:
    - [ ] **Tenant Settings**: Add `takeawayEnabled` (boolean) to the Tenant configuration database.
    - [ ] **Checkout Validation**: If enabled, display the takeaway option. Require `customerName` and `customerContact` (validated email/phone).
    - [ ] **Cart Flow**: Set `OrderChannel` to `TAKEAWAY` on order creation.

---

## 3. Delivery Ordering
*   **Goal**: Allow customers to select delivery, requiring contact info, address, and pre-payment via Stripe checkout. Only available if delivery is enabled by the tenant.
*   **Key Tasks**:
    - [ ] **Tenant Settings**: Add `deliveryEnabled` (boolean) to the Tenant configuration.
    - [ ] **Checkout Form**: If enabled, present address fields (Street, City, Zip) and contact info.
    - [ ] **Pre-Payment Lock**: Force selection of Stripe checkout (disabled cash/at-counter option for delivery). Do not mark the order as `ACCEPTED` or send it to the kitchen until Stripe sends the `paid` status confirmation webhook.

---

## 4. CSS Harmonization & Theme Support
*   **Goal**: Restructure CSS files to prepare for multiple themes (Café, FastFood, Modern Restaurant), keeping the current design as the default.
*   **Key Tasks**:
    - [ ] **CSS Variables Migration**: Extract static styling values (colors, fonts, border-radius, shadows) from `style.css` and move them into a `:root` block of CSS custom properties.
    - [ ] **Thymeleaf Integration**: Inject custom `:root` property definitions inline via a global Thymeleaf layout header based on the active Tenant's theme selections.
    - [ ] **Money Configurations**: Keep currency, cash denominations, and payment configs globally consistent regardless of active visual themes.

---

## 5. Theme: Café
*   **Goal**: Implement a Café visual layout and provide matching demo data.
*   **Key Tasks**:
    - [ ] **Café CSS variables**: Muted browns, cream background, serif typography (e.g. *Playfair Display*).
    - [ ] **Café Demo Data**: Build `cafe_demo.json` containing:
        - Product groups: Hot Coffee, Iced Coffee, Bakery, Tea.
        - Customizations: Milk Selection (Whole, Oat, Almond, Soy), Extra Espresso Shots, Sweeteners.
        - High-quality beverage images.

---

## 6. Theme: Modern Restaurant
*   **Goal**: Implement a sleek, upscale dining theme with matching demo data.
*   **Key Tasks**:
    - [ ] **Modern Restaurant styling variables**: Clean dark mode or high-contrast minimalist styling, thin sans-serif typography (e.g. *Outfit* or *Inter*).
    - [ ] **Modern Restaurant Demo Data**: Build `modern_restaurant_demo.json` containing:
        - Product groups: Starters, Main Course, Desserts, Fine Wine.
        - Customizations: Meat Temp (Rare, Medium, Well), Side Dishes.

---

## 7. Simple Multi-Tenant Implementation
*   **Goal**: Implement path-based multi-tenancy where all existing URLs start with `/{tenantId}` (e.g. `/{tenantId}/menu`, `/{tenantId}/dashboard`).
*   **Key Tasks**:
    - [ ] **Tenant Routing Filter / Interceptor**:
        - Write a `TenantFilter` that parses the first path segment.
        - Bind the extracted `tenantId` to a `ThreadLocal` `TenantContext`.
        - Wrap the HttpServletRequest to forward the request internally to the non-tenant URL (keeping controller mappings clean).
    - [ ] **Security Data Initializer Adjustment**:
        - In [SecurityDataInitializer.java](file:///d:/git/fastbite/fastbite/adapter-in/web/src/main/java/es/brasatech/fastbite/security/SecurityDataInitializer.java), initialize a default tenant (e.g., `default-store`) and bind the initial staff users (`admin`, `manager`, `cashier`, etc.) to this default tenant.
    - [ ] **Database Filters**:
        - JPA: Configure `@TenantId` on JPA entities to automatically scope queries by the active tenant.
        - MongoDB: Inject `tenantId` parameters dynamically in service queries.

---

## 8. Signup Platform & Dynamic Promos
*   **Goal**: Establish a public marketing landing page, help docs, a developer architecture showcase, and a dynamic signup page.
*   **Key Tasks**:
    - [ ] **Landing Page**: Add promotional sections, pricing tiers, and direct Calls-to-Action (CTA).
    - [ ] **User Signup Form**: Gather Tenant ID slug, email, and password. Redirect to Stripe Checkout for subscription.
    - [ ] **Tenant Documentation**: A user-friendly manual detailing store layout setup, customization option configs, and dashboard monitoring.
    - [ ] **Architecture Page**: Highlighting system performance (GraalVM native compiles, Hexagonal architecture pattern, Thymeleaf SSR, WebSocket integrations).
    - [ ] **Automation / Screenshots Prompt**:
        - Use the `/browser` slash command to run integration UI tests.
        - Capture screenshots of the Counter POS, Kitchen Dashboard, and Customer Menu using the web agent, to dynamically render real-time preview images on the landing page.
