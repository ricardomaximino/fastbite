package es.brasatech.fastbite.domain.customization;

import es.brasatech.fastbite.domain.I18nField;

import java.util.List;
import java.util.stream.Collectors;

/**
 * I18n-aware Customization DTO with translatable fields.
 * This version stores translations for all supported locales.
 */
public record CustomizationI18n(
                String id,
                I18nField name,
                String type,
                List<CustomizationOptionI18n> options,
                int usageCount) {
        /**
         * Create from regular CustomizationDto (converts default language values to
         * I18nField)
         */
        public static CustomizationI18n fromCustomizationDto(CustomizationDto customization, String defaultLocale) {
                return new CustomizationI18n(
                                customization.id(),
                                I18nField.of(defaultLocale, customization.name()),
                                customization.type(),
                                customization.options().stream()
                                                .map(opt -> CustomizationOptionI18n.fromCustomizationOptionDto(opt,
                                                                defaultLocale))
                                                .collect(Collectors.toList()),
                                customization.usageCount());
        }

        /**
         * Convert to regular CustomizationDto for specific locale with fallback
         */
        public CustomizationDto toCustomizationDto(String locale, String defaultLocale) {
                return new CustomizationDto(
                                id,
                                name.get(locale, defaultLocale),
                                type,
                                options.stream()
                                                .map(opt -> opt.toCustomizationOptionDto(locale, defaultLocale))
                                                .collect(Collectors.toList()),
                                usageCount);
        }

        /**
         * Convert to regular CustomizationDto using default locale
         */
        public CustomizationDto toCustomizationDto(String defaultLocale) {
                return toCustomizationDto(defaultLocale, defaultLocale);
        }
}
