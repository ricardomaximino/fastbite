package es.brasatech.fastbite.jpa.product;

import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.office.ProductService;
import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.product.ProductDto;
import es.brasatech.fastbite.domain.product.ProductI18n;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * JPA implementation of ProductService for relational databases (PostgreSQL,
 * MySQL).
 * Uses separate translation tables for non-default languages.
 * Active when profile 'jpa' is enabled.
 */
@Service
@Profile("jpa")
@RequiredArgsConstructor
@Transactional
public class ProductServiceJpaImpl implements ProductService {

    private final ProductJpaRepository repository;
    private final ProductTranslationJpaRepository translationRepository;
    private final I18nConfig i18NConfig;

    @Override
    public List<ProductDto> findAll() {
        Locale locale = LocaleContextHolder.getLocale();
        String requestedLang = locale.getLanguage();
        String defaultLang = i18NConfig.getDefaultLanguage();

        // Fast path: if requested == default, no translation needed
        if (requestedLang.equals(defaultLang)) {
            return repository.findAll().stream()
                    .map(entity -> toDto(entity))
                    .toList();
        }

        // Load entities and apply translations
        return repository.findAll().stream()
                .map(entity -> toDtoWithTranslation(entity, requestedLang, defaultLang))
                .toList();
    }

    @Override
    public Optional<ProductDto> findById(String id) {
        Locale locale = LocaleContextHolder.getLocale();
        String requestedLang = locale.getLanguage();
        String defaultLang = i18NConfig.getDefaultLanguage();

        return repository.findById(id)
                .map(entity -> {
                    // Fast path: if requested == default, no translation needed
                    if (requestedLang.equals(defaultLang)) {
                        return toDto(entity);
                    }
                    // Apply translation with fallback
                    return toDtoWithTranslation(entity, requestedLang, defaultLang);
                });
    }

    @Override
    public ProductDto create(ProductDto productDto) {
        ProductEntity entity = new ProductEntity(
                null, // JPA will generate ID
                productDto.name(),
                productDto.price(),
                productDto.description(),
                productDto.image(),
                productDto.customizations() != null ? new ArrayList<>(productDto.customizations()) : new ArrayList<>(),
                productDto.active());
        ProductEntity saved = repository.save(entity);
        return toDto(saved);
    }

    @Override
    public Optional<ProductDto> update(String id, ProductDto productDto) {
        Optional<ProductEntity> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        // Update default language values directly
        ProductEntity entity = new ProductEntity(
                id,
                productDto.name(),
                productDto.price(),
                productDto.description(),
                productDto.image(),
                productDto.customizations() != null ? new ArrayList<>(productDto.customizations()) : new ArrayList<>(),
                productDto.active());
        ProductEntity saved = repository.save(entity);
        return Optional.of(toDto(saved));
    }

    @Override
    public boolean delete(String id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    @Override
    public void clear() {
        repository.deleteAll();
    }

    // ===== I18n Methods =====

    @Override
    public Optional<ProductI18n> findI18nById(String id) {
        return repository.findById(id)
                .map(entity -> {
                    // Load all translations for this product
                    List<ProductTranslationEntity> translations = translationRepository.findAllByProductId(id);
                    return toI18nDto(entity, translations);
                });
    }

    @Override
    public Optional<ProductI18n> updateI18n(String id, ProductI18n productI18n) {
        Optional<ProductEntity> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        String defaultLang = i18NConfig.getDefaultLanguage();

        // Update default language values in main entity
        ProductEntity entity = new ProductEntity(
                id,
                productI18n.name().get(defaultLang, defaultLang),
                productI18n.price(),
                productI18n.description().get(defaultLang, defaultLang),
                productI18n.image(),
                productI18n.customizations() != null ? new ArrayList<>(productI18n.customizations())
                        : new ArrayList<>(),
                productI18n.active());
        ProductEntity saved = repository.save(entity);

        // Update translations for non-default languages
        updateTranslations(id, productI18n.name(), productI18n.description(), defaultLang);

        // Load all translations to return
        List<ProductTranslationEntity> translations = translationRepository.findAllByProductId(id);
        return Optional.of(toI18nDto(saved, translations));
    }

    @Override
    public Optional<ProductDto> findByIdInLocale(String id, String locale) {
        String defaultLang = i18NConfig.getDefaultLanguage();
        return repository.findById(id)
                .map(entity -> {
                    // Fast path: if requested == default, no translation needed
                    if (locale.equals(defaultLang)) {
                        return toDto(entity);
                    }
                    // Apply translation with fallback
                    return toDtoWithTranslation(entity, locale, defaultLang);
                });
    }

    @Override
    public List<ProductDto> findAllInLocale(String locale) {
        String defaultLang = i18NConfig.getDefaultLanguage();

        // Fast path: if requested == default, no translation needed
        if (locale.equals(defaultLang)) {
            return repository.findAll().stream()
                    .map(this::toDto)
                    .toList();
        }

        // Load entities and apply translations
        return repository.findAll().stream()
                .map(entity -> toDtoWithTranslation(entity, locale, defaultLang))
                .toList();
    }

    // ===== Converter Methods =====

    /**
     * Convert entity to DTO using default language values (fast path).
     */
    private ProductDto toDto(ProductEntity entity) {
        return new ProductDto(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getDescription(),
                entity.getImage(),
                entity.getCustomizations(),
                entity.isActive());
    }

    /**
     * Convert entity to DTO with translation fallback.
     * Implements field-level fallback: translation field → default field → full
     * fallback.
     */
    private ProductDto toDtoWithTranslation(ProductEntity entity, String requestedLang, String defaultLang) {
        // Try to load translation
        Optional<ProductTranslationEntity> translationOpt = translationRepository.findByProductIdAndLanguage(
                entity.getId(), requestedLang);

        if (translationOpt.isPresent()) {
            ProductTranslationEntity translation = translationOpt.get();
            // Field-level fallback: use translation if present, otherwise fallback to
            // default
            return new ProductDto(
                    entity.getId(),
                    fallback(translation.getName(), entity.getName()),
                    entity.getPrice(),
                    fallback(translation.getDescription(), entity.getDescription()),
                    entity.getImage(),
                    entity.getCustomizations(),
                    entity.isActive());
        }

        // Full fallback: no translation found, use default language values
        return toDto(entity);
    }

    /**
     * Field-level fallback helper.
     * Returns value if non-null, otherwise returns defaultValue.
     */
    private String fallback(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Convert entity and translations to I18n DTO.
     * Builds I18nField objects from default language values and translations.
     */
    private ProductI18n toI18nDto(ProductEntity entity, List<ProductTranslationEntity> translations) {
        String defaultLang = i18NConfig.getDefaultLanguage();

        // Build name I18nField (default language + translations)
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put(defaultLang, entity.getName());
        for (ProductTranslationEntity translation : translations) {
            if (translation.getName() != null) {
                nameMap.put(translation.getLanguage(), translation.getName());
            }
        }
        I18nField name = new I18nField(nameMap);

        // Build description I18nField (default language + translations)
        Map<String, String> descriptionMap = new HashMap<>();
        descriptionMap.put(defaultLang, entity.getDescription());
        for (ProductTranslationEntity translation : translations) {
            if (translation.getDescription() != null) {
                descriptionMap.put(translation.getLanguage(), translation.getDescription());
            }
        }
        I18nField description = new I18nField(descriptionMap);

        return new ProductI18n(
                entity.getId(),
                name,
                entity.getPrice(),
                description,
                entity.getImage(),
                entity.getCustomizations(),
                entity.isActive());
    }

    /**
     * Update translations for non-default languages.
     * Deletes old translations and creates new ones based on I18nField contents.
     */
    private void updateTranslations(String productId, I18nField nameField, I18nField descriptionField,
            String defaultLang) {
        // Delete existing translations
        translationRepository.deleteByProductId(productId);

        // Get the product entity for reference
        ProductEntity product = repository.findById(productId).orElseThrow();

        // Create new translations for all non-default languages
        Map<String, String> nameTranslations = nameField.getAll();
        Map<String, String> descriptionTranslations = descriptionField.getAll();

        // Collect all languages (from both name and description)
        var allLanguages = new java.util.HashSet<String>();
        allLanguages.addAll(nameTranslations.keySet());
        allLanguages.addAll(descriptionTranslations.keySet());
        allLanguages.remove(defaultLang); // Don't create translation for default language

        for (String lang : allLanguages) {
            ProductTranslationEntity translation = new ProductTranslationEntity(
                    product,
                    lang,
                    nameTranslations.get(lang),
                    descriptionTranslations.get(lang));
            translationRepository.save(translation);
        }
    }
}
