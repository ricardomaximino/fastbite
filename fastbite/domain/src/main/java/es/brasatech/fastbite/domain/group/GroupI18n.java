package es.brasatech.fastbite.domain.group;

import es.brasatech.fastbite.domain.I18nField;

import java.util.List;

/**
 * I18n-aware Group record with translatable fields.
 * This version stores translations for all supported locales.
 */
public record GroupI18n(
        String id,
        I18nField name,
        I18nField description,
        String icon,
        List<String> products) {
    /**
     * Create from regular Group (converts default language values to I18nField)
     */
    public static GroupI18n fromGroup(Group group, String defaultLocale) {
        return new GroupI18n(
                group.id(),
                I18nField.of(defaultLocale, group.name()),
                I18nField.of(defaultLocale, group.description()),
                group.icon(),
                group.products());
    }

    /**
     * Convert to regular Group for specific locale with fallback
     */
    public Group toGroup(String locale, String defaultLocale) {
        return new Group(
                id,
                name.get(locale, defaultLocale),
                description.get(locale, defaultLocale),
                icon,
                products);
    }

    /**
     * Convert to regular Group using default locale
     */
    public Group toGroup(String defaultLocale) {
        return toGroup(defaultLocale, defaultLocale);
    }
}
