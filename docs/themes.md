# Themes and guest preview

Only owners configure appearance, from their workspace:

- Account settings: save the owner default for current and future locations that inherit it.
- Location settings → Appearance: use the owner default or choose a location override.
- Menu, counter, kitchen, checkout and other restaurant pages resolve the theme on the server. Browser-local theme settings no longer override it.

Existing locations initially use FastFood. A removed theme falls back to FastFood until the owner chooses a replacement. Demo loading does not change appearance; catalog templates and theme preferences are independent. Appearance is platform configuration and is not included in restaurant backup ZIPs.

## Install a theme

Add a folder under `themes/` in the application's working directory (or configure `fastbite.themes.directory` / `FASTBITE_THEMES_DIRECTORY`). Each trusted operator-installed package contains:

```text
themes/coastal/
  theme.properties
  theme.css
  menu.svg
  counter.svg
  kitchen.svg
```

Example UTF-8 `theme.properties`:

```properties
id=coastal
name=Coastal
description=Fresh colours for relaxed seaside dining.
```

The ID must start with a lowercase letter and contain only lowercase letters, digits and hyphens, up to 64 characters. IDs are stable saved preferences. `theme.css` loads after shared restaurant CSS; define CSS variables and component overrides under `[data-theme="coastal"]`. See the three bundled packages in `fastbite/adapter-in/web/src/main/resources/themes` and the variables in `static/css/style.css`.

The three SVG illustrations represent menu, counter and kitchen views. Use a 380 × 190 view box and label them as illustrative; the live preview displays actual restaurant content. Packages are trusted deployment assets, not user uploads. Do not include scripts, external tracking or imports. Only the four declared asset filenames are served; manifests and arbitrary files are not public.

Restart after installing packages. No Java changes or native rebuild are needed when mounting an external directory into `/app/themes` in Docker. External packages can override a bundled ID. Incomplete packages fail startup with an explicit diagnostic. To ship a new built-in package, place it in the classpath `themes` directory and rebuild; native resource hints include the directory automatically.

## Demo templates

Gemini's external `demo-templates/*.zip` packages remain supported. The native Docker image ships that directory. Classpath archives are listed in `demo-templates.list`, including café, kebab and Michelin. Add bundled archives to that list; native metadata includes `*_demo.zip` without Java edits.

## Inline guest preview

The opening checklist and daily-tools menu action open a same-origin iframe dialog. Mobile and desktop controls change its viewport width. Escape or Close dismisses it and restores keyboard focus; opening a new tab remains available. The current location's saved theme is applied, and staff navigation is hidden in guest preview mode. This is a live menu, not a transaction sandbox: orders placed there are real. The dialog explicitly says so. Existing same-origin frame protection is retained.

The platform favicon is `/images/favicon.svg`, based on the orange FastBite `f.` mark.
