package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.tenant.AppearancePort;
import es.brasatech.fastbite.config.ThemeCatalog;
import es.brasatech.fastbite.domain.tenant.TenantContext;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AppearanceAdvice {
    private final AppearancePort preferences;
    private final ThemeCatalog themes;
    public AppearanceAdvice(AppearancePort preferences,ThemeCatalog themes) { this.preferences=preferences;this.themes=themes; }
    @ModelAttribute("locationTheme")
    public ThemeCatalog.Theme theme() { return themes.resolve(preferences.effectiveTheme(TenantContext.getCurrentTenant())); }
}
