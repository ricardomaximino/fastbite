# Owner workspace

Appearance preferences, theme packages, the platform favicon and the inline live guest preview are described in [Themes and guest preview](themes.md).

The owner console now has a shared sidebar, a location selector and addressable Overview, Locations, Team, Location settings and Account & billing pages. Existing counter, kitchen and menu tools remain linked from Overview. Locations is the portfolio view; future aggregate reporting can add an All locations scope when real metrics are implemented. Insights and Marketing are intentionally not shown before they exist.

New restaurants can load the bundled sample restaurant, restore a FastBite ZIP or start their own menu. The opening checklist derives menu readiness from actual products. Service review is recorded only after a successful settings save. Guest-menu review is an explicit owner acknowledgement, not an inferred page visit. Dismiss/resume and review state persist per location in public.owner_onboarding (platform migration V5), independently of browser sessions and restaurant backups. Owners may always reopen the checklist. Staff setup is optional.

The sample is the existing bundled restaurant archive, not a new cafe-specific template. It replaces restaurant data and clears existing staff/orders, but does not import sample accounts or historical orders. Guest-menu preview opens the real menu and is labelled accordingly.

Backup preflight uses the same archive parser, format checks, media path checks and expanded-size limit as restoration. It stages media temporarily and removes it without replacing destination data. It returns only counts, not private record details. The browser submits the exact File object reviewed. Restored account tenant IDs are rebound to the destination restaurant, so account metadata matches the restaurant receiving the backup. Restoration validates again and retains existing transaction/media rollback behavior. Preflight is structural validation, not a guarantee that all database constraints will pass.

Demo import, restore and reset use a dialog explaining scope and replacement, a backup download link and an exact location-name confirmation. No replacement happens on choosing a file or inspecting a backup. Server ownership/role authorization and CSRF remain enforced independently of the UI. The new preview, progress and service endpoints explicitly verify location ownership.

Diner payments and optional domain mapping live under location settings; platform subscription billing and volume pricing live under Account & billing. Saving a Stripe account ID does not verify Stripe onboarding or payment readiness. No live Stripe or DNS setup is performed. Billing dates display readable dates/times with UTC explicitly labelled.

The workspace uses the public site's palette and fonts with compact application headings, responsive navigation, labelled inputs, visible focus, native disclosure/dialog controls and status feedback. Future features should reuse the shell and explicit location context, without introducing nonfunctional navigation.
