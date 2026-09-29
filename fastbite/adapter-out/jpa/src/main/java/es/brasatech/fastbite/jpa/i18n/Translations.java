package es.brasatech.fastbite.jpa.i18n;

import es.brasatech.fastbite.domain.I18nField;

import java.util.HashMap;
import java.util.Map;

/**
 * Helpers for an entity's {@code translations} column: language -> field -> text.
 * The default language lives in the entity's own columns; this map only holds the other languages.
 * Always assign a new map to the entity instead of mutating it, so Hibernate sees the change.
 */
public final class Translations {

    private Translations() {
    }

    /** The field in the given language, falling back to the default-language value. */
    public static String get(Map<String, Map<String, String>> translations, String language, String field,
                             String defaultValue) {
        Map<String, String> fields = translations != null ? translations.get(language) : null;
        String value = fields != null ? fields.get(field) : null;
        return value != null && !value.isBlank() ? value : defaultValue;
    }

    /** All languages of one field, including the default-language value. */
    public static I18nField toI18nField(Map<String, Map<String, String>> translations, String field,
                                        String defaultLanguage, String defaultValue) {
        Map<String, String> values = new HashMap<>();
        if (translations != null) {
            translations.forEach((language, fields) -> {
                String value = fields.get(field);
                if (value != null) {
                    values.put(language, value);
                }
            });
        }
        values.put(defaultLanguage, defaultValue);
        return new I18nField(values);
    }

    /** Builds the translations map from translatable fields, keeping only the non-default languages. */
    public static Map<String, Map<String, String>> of(String defaultLanguage, Map<String, I18nField> fields) {
        Map<String, Map<String, String>> translations = new HashMap<>();
        fields.forEach((field, i18n) -> {
            if (i18n == null) {
                return;
            }
            i18n.getAll().forEach((language, value) -> {
                if (!language.equals(defaultLanguage) && value != null && !value.isBlank()) {
                    translations.computeIfAbsent(language, l -> new HashMap<>()).put(field, value);
                }
            });
        });
        return translations;
    }
}
