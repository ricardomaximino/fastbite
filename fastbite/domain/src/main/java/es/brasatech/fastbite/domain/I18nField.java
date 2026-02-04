package es.brasatech.fastbite.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a translatable field that supports multiple languages.
 * Uses a Map to store translations with locale codes as keys.
 * Supports automatic fallback to default language.
 */
public class I18nField {

    @JsonValue
    private final Map<String, String> translations;

    public I18nField() {
        this.translations = new HashMap<>();
    }

    @JsonCreator
    public I18nField(Map<String, String> translations) {
        this.translations = translations != null ? new HashMap<>(translations) : new HashMap<>();
    }

    /**
     * Create I18nField with default language value
     */
    public static I18nField of(String defaultLocale, String value) {
        I18nField field = new I18nField();
        field.set(defaultLocale, value);
        return field;
    }

    /**
     * Get translation for specific locale with fallback to default
     */
    public String get(String locale, String defaultLocale) {
        String value = translations.get(locale);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        // Fallback to default locale
        if (!locale.equals(defaultLocale)) {
            value = translations.get(defaultLocale);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        // Fallback to any available translation
        return translations.values().stream()
                .filter(v -> v != null && !v.isEmpty())
                .findFirst()
                .orElse("");
    }

    /**
     * Get translation for default locale
     */
    public String getDefault(String defaultLocale) {
        return translations.getOrDefault(defaultLocale, "");
    }

    /**
     * Set translation for specific locale
     */
    public void set(String locale, String value) {
        if (value != null && !value.trim().isEmpty()) {
            translations.put(locale, value.trim());
        } else {
            translations.remove(locale);
        }
    }

    /**
     * Check if translation exists for locale
     */
    public boolean has(String locale) {
        String value = translations.get(locale);
        return value != null && !value.isEmpty();
    }

    /**
     * Get all translations
     */
    public Map<String, String> getAll() {
        return new HashMap<>(translations);
    }

    /**
     * Check if field has any translations
     */
    public boolean isEmpty() {
        return translations.isEmpty() ||
                translations.values().stream().allMatch(v -> v == null || v.isEmpty());
    }

    /**
     * Check if all specified locales have translations
     */
    public boolean hasAllLocales(Iterable<String> locales) {
        for (String locale : locales) {
            if (!has(locale)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        I18nField i18nField = (I18nField) o;
        return Objects.equals(translations, i18nField.translations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(translations);
    }

    @Override
    public String toString() {
        return translations.toString();
    }
}
