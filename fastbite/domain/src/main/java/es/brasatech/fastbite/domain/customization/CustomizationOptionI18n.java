package es.brasatech.fastbite.domain.customization;

import es.brasatech.fastbite.domain.I18nField;

import java.math.BigDecimal;

/**
 * I18n-aware CustomizationOption DTO with translatable fields.
 * This version stores translations for all supported locales.
 */
public record CustomizationOptionI18n(
        String id,
        I18nField name,
        BigDecimal price,
        boolean isSelectedByDefault,
        int defaultValue) {
    /**
     * Create from regular CustomizationOptionDto (converts default language values
     * to I18nField)
     */
    public static CustomizationOptionI18n fromCustomizationOptionDto(CustomizationOptionDto option,
            String defaultLocale) {
        return new CustomizationOptionI18n(
                option.id(),
                I18nField.of(defaultLocale, option.name()),
                option.price(),
                option.isSelectedByDefault(),
                option.defaultValue());
    }

    /**
     * Convert to regular CustomizationOptionDto for specific locale with fallback
     */
    public CustomizationOptionDto toCustomizationOptionDto(String locale, String defaultLocale) {
        return new CustomizationOptionDto(
                id,
                name.get(locale, defaultLocale),
                price,
                isSelectedByDefault,
                defaultValue);
    }

    /**
     * Convert to regular CustomizationOptionDto using default locale
     */
    public CustomizationOptionDto toCustomizationOptionDto(String defaultLocale) {
        return toCustomizationOptionDto(defaultLocale, defaultLocale);
    }
}
