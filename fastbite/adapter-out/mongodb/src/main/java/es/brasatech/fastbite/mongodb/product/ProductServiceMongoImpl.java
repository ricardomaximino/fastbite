package es.brasatech.fastbite.mongodb.product;

import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.office.ProductService;
import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.product.ProductDto;
import es.brasatech.fastbite.domain.product.ProductI18n;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * MongoDB implementation of ProductService for NoSQL databases.
 * Uses separate translation collection for non-default languages.
 * Active when profile 'mongodb' is enabled.
 */
@Service
@Profile("mongodb")
@RequiredArgsConstructor
public class ProductServiceMongoImpl implements ProductService {

    private final ProductMongoRepository repository;
    private final ProductTranslationMongoRepository translationRepository;
    private final I18nConfig i18NConfig;

    @Override
    public List<ProductDto> findAll() {
        Locale locale = LocaleContextHolder.getLocale();
        String requestedLang = locale.getLanguage();
        String defaultLang = i18NConfig.getDefaultLanguage();

        // Fast path: if requested == default, no translation needed
        if (requestedLang.equals(defaultLang)) {
            return repository.findAll().stream()
                    .map(document -> toDto(document))
                    .toList();
        }

        // Load documents and apply translations
        return repository.findAll().stream()
                .map(document -> toDtoWithTranslation(document, requestedLang, defaultLang))
                .toList();
    }

    @Override
    public Optional<ProductDto> findById(String id) {
        Locale locale = LocaleContextHolder.getLocale();
        String requestedLang = locale.getLanguage();
        String defaultLang = i18NConfig.getDefaultLanguage();

        return repository.findById(id)
                .map(document -> {
                    // Fast path: if requested == default, no translation needed
                    if (requestedLang.equals(defaultLang)) {
                        return toDto(document);
                    }
                    // Apply translation with fallback
                    return toDtoWithTranslation(document, requestedLang, defaultLang);
                });
    }

    @Override
    public ProductDto create(ProductDto productDto) {
        ProductDocument document = new ProductDocument(
                null, // MongoDB will generate ID
                productDto.name(),
                productDto.price(),
                productDto.description(),
                productDto.image(),
                productDto.customizations() != null ? new ArrayList<>(productDto.customizations()) : new ArrayList<>(),
                productDto.active());
        ProductDocument saved = repository.save(document);
        return toDto(saved);
    }

    @Override
    public Optional<ProductDto> update(String id, ProductDto productDto) {
        Optional<ProductDocument> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        // Update default language values directly
        ProductDocument document = new ProductDocument(
                id,
                productDto.name(),
                productDto.price(),
                productDto.description(),
                productDto.image(),
                productDto.customizations() != null ? new ArrayList<>(productDto.customizations()) : new ArrayList<>(),
                productDto.active());
        ProductDocument saved = repository.save(document);
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
                .map(document -> {
                    // Load all translations for this product
                    List<ProductTranslationDocument> translations = translationRepository.findAllByProductId(id);
                    return toI18nDto(document, translations);
                });
    }

    @Override
    public Optional<ProductI18n> updateI18n(String id, ProductI18n productI18n) {
        Optional<ProductDocument> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        String defaultLang = i18NConfig.getDefaultLanguage();

        // Update default language values in main document
        ProductDocument document = new ProductDocument(
                id,
                productI18n.name().get(defaultLang, defaultLang),
                productI18n.price(),
                productI18n.description().get(defaultLang, defaultLang),
                productI18n.image(),
                productI18n.customizations() != null ? new ArrayList<>(productI18n.customizations())
                        : new ArrayList<>(),
                productI18n.active());
        ProductDocument saved = repository.save(document);

        // Update translations for non-default languages
        updateTranslations(id, productI18n.name(), productI18n.description(), defaultLang);

        // Load all translations to return
        List<ProductTranslationDocument> translations = translationRepository.findAllByProductId(id);
        return Optional.of(toI18nDto(saved, translations));
    }

    @Override
    public Optional<ProductDto> findByIdInLocale(String id, String locale) {
        String defaultLang = i18NConfig.getDefaultLanguage();
        return repository.findById(id)
                .map(document -> {
                    // Fast path: if requested == default, no translation needed
                    if (locale.equals(defaultLang)) {
                        return toDto(document);
                    }
                    // Apply translation with fallback
                    return toDtoWithTranslation(document, locale, defaultLang);
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

        // Load documents and apply translations
        return repository.findAll().stream()
                .map(document -> toDtoWithTranslation(document, locale, defaultLang))
                .toList();
    }

    // ===== Converter Methods =====

    /**
     * Convert document to DTO using default language values (fast path).
     */
    private ProductDto toDto(ProductDocument document) {
        return new ProductDto(
                document.getId(),
                document.getName(),
                document.getPrice(),
                document.getDescription(),
                document.getImage(),
                document.getCustomizations(),
                document.isActive());
    }

    /**
     * Convert document to DTO with translation fallback.
     * Implements field-level fallback: translation field → default field → full
     * fallback.
     */
    private ProductDto toDtoWithTranslation(ProductDocument document, String requestedLang, String defaultLang) {
        // Try to load translation
        Optional<ProductTranslationDocument> translationOpt = translationRepository.findByProductIdAndLanguage(
                document.getId(), requestedLang);

        if (translationOpt.isPresent()) {
            ProductTranslationDocument translation = translationOpt.get();
            // Field-level fallback: use translation if present, otherwise fallback to
            // default
            return new ProductDto(
                    document.getId(),
                    fallback(translation.getName(), document.getName()),
                    document.getPrice(),
                    fallback(translation.getDescription(), document.getDescription()),
                    document.getImage(),
                    document.getCustomizations(),
                    document.isActive());
        }

        // Full fallback: no translation found, use default language values
        return toDto(document);
    }

    /**
     * Field-level fallback helper.
     * Returns value if non-null, otherwise returns defaultValue.
     */
    private String fallback(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Convert document and translations to I18n DTO.
     * Builds I18nField objects from default language values and translations.
     */
    private ProductI18n toI18nDto(ProductDocument document, List<ProductTranslationDocument> translations) {
        String defaultLang = i18NConfig.getDefaultLanguage();

        // Build name I18nField (default language + translations)
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put(defaultLang, document.getName());
        for (ProductTranslationDocument translation : translations) {
            if (translation.getName() != null) {
                nameMap.put(translation.getLanguage(), translation.getName());
            }
        }
        I18nField name = new I18nField(nameMap);

        // Build description I18nField (default language + translations)
        Map<String, String> descriptionMap = new HashMap<>();
        descriptionMap.put(defaultLang, document.getDescription());
        for (ProductTranslationDocument translation : translations) {
            if (translation.getDescription() != null) {
                descriptionMap.put(translation.getLanguage(), translation.getDescription());
            }
        }
        I18nField description = new I18nField(descriptionMap);

        return new ProductI18n(
                document.getId(),
                name,
                document.getPrice(),
                description,
                document.getImage(),
                document.getCustomizations(),
                document.isActive());
    }

    /**
     * Update translations for non-default languages.
     * Deletes old translations and creates new ones based on I18nField contents.
     */
    private void updateTranslations(String productId, I18nField nameField, I18nField descriptionField,
            String defaultLang) {
        // Delete existing translations
        translationRepository.deleteByProductId(productId);

        // Create new translations for all non-default languages
        Map<String, String> nameTranslations = nameField.getAll();
        Map<String, String> descriptionTranslations = descriptionField.getAll();

        // Collect all languages (from both name and description)
        var allLanguages = new java.util.HashSet<String>();
        allLanguages.addAll(nameTranslations.keySet());
        allLanguages.addAll(descriptionTranslations.keySet());
        allLanguages.remove(defaultLang); // Don't create translation for default language

        for (String lang : allLanguages) {
            ProductTranslationDocument translation = new ProductTranslationDocument(
                    productId,
                    lang,
                    nameTranslations.get(lang),
                    descriptionTranslations.get(lang));
            translationRepository.save(translation);
        }
    }
}
