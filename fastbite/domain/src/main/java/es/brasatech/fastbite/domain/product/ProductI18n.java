package es.brasatech.fastbite.domain.product;


import es.brasatech.fastbite.domain.I18nField;

import java.math.BigDecimal;
import java.util.List;

/**
 * I18n-aware Product DTO with translatable fields.
 * This version stores translations for all supported locales.
 */
public record ProductI18n(
        String id,
        I18nField name,
        BigDecimal price,
        I18nField description,
        String image,
        List<String> customizations,
        boolean active) {
    /**
     * Create from regular ProductDto (converts default language values to
     * I18nField)
     */
    public static ProductI18n fromProductDto(ProductDto product, String defaultLocale) {
        return new ProductI18n(
                product.id(),
                I18nField.of(defaultLocale, product.name()),
                product.price(),
                I18nField.of(defaultLocale, product.description()),
                product.image(),
                product.customizations(),
                product.active());
    }

    /**
     * Convert to regular ProductDto for specific locale with fallback
     */
    public ProductDto toProductDto(String locale, String defaultLocale) {
        return new ProductDto(
                id,
                name.get(locale, defaultLocale),
                price,
                description.get(locale, defaultLocale),
                image,
                customizations,
                active);
    }

    /**
     * Convert to regular ProductDto using default locale
     */
    public ProductDto toProductDto(String defaultLocale) {
        return toProductDto(defaultLocale, defaultLocale);
    }
}
