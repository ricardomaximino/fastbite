package es.brasatech.fastbite.application.office;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for internationalization.
 * Reads i18n.default-language and i18n.supported-locales from application.yml.
 */
public class I18nConfig {

    /**
     * Default language for entity text fields.
     * Values in main entity tables are stored in this language.
     * Translations for other languages are in separate translation tables.
     */
    private String defaultLanguage = "en";

    /**
     * List of supported locales for translation UI.
     * Add or remove locales as needed.
     */
    private List<String> supportedLocales = new ArrayList<>(List.of("en", "es", "pt", "fr", "de"));

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public List<String> getSupportedLocales() {
        return supportedLocales;
    }

    public void setSupportedLocales(List<String> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }
}
