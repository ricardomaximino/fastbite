package es.brasatech.fastbite.mongodb.customization;

import es.brasatech.fastbite.application.office.CustomizationService;
import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.customization.CustomizationDto;
import es.brasatech.fastbite.domain.customization.CustomizationI18n;
import es.brasatech.fastbite.domain.customization.CustomizationOptionDto;
import es.brasatech.fastbite.domain.customization.CustomizationOptionI18n;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.IntStream;

/**
 * MongoDB implementation of CustomizationService for NoSQL databases.
 * Uses separate translation collections for non-default languages.
 * Active when profile 'mongodb' is enabled.
 */
@Service
@Profile("mongodb")
@RequiredArgsConstructor
public class CustomizationServiceMongoImpl implements CustomizationService {

    private final CustomizationMongoRepository repository;
    private final CustomizationTranslationMongoRepository translationRepository;
    private final CustomizationOptionTranslationMongoRepository optionTranslationRepository;
    private final I18nConfig i18NConfig;

    @Override
    public List<CustomizationDto> findAll() {
        Locale locale = LocaleContextHolder.getLocale();
        String requestedLang = locale.getLanguage();
        String defaultLang = i18NConfig.getDefaultLanguage();

        // Fast path: if requested == default, no translation needed
        if (requestedLang.equals(defaultLang)) {
            return repository.findAll().stream()
                    .map(this::toDto)
                    .toList();
        }

        // Load documents and apply translations
        return repository.findAll().stream()
                .map(document -> toDtoWithTranslation(document, requestedLang, defaultLang))
                .toList();
    }

    @Override
    public Optional<CustomizationDto> findById(String id) {
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
    public CustomizationDto create(CustomizationDto customizationDto) {
        CustomizationDocument document = new CustomizationDocument(
                null, // MongoDB will generate ID
                customizationDto.name(),
                customizationDto.type(),
                customizationDto.options() != null ? new ArrayList<>(customizationDto.options()) : new ArrayList<>(),
                0 // Initial usage count
        );
        CustomizationDocument saved = repository.save(document);
        return toDto(saved);
    }

    @Override
    public Optional<CustomizationDto> update(String id, CustomizationDto customizationDto) {
        Optional<CustomizationDocument> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        CustomizationDocument existing = existingOpt.get();

        // Update default language values directly
        CustomizationDocument document = new CustomizationDocument(
                id,
                customizationDto.name(),
                customizationDto.type(),
                customizationDto.options() != null ? new ArrayList<>(customizationDto.options()) : new ArrayList<>(),
                existing.getUsageCount() // Preserve usage count
        );
        CustomizationDocument saved = repository.save(document);
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
    public Optional<CustomizationI18n> findI18nById(String id) {
        return repository.findById(id)
                .map(document -> {
                    // Load all translations for this customization
                    List<CustomizationTranslationDocument> translations = translationRepository
                            .findAllByCustomizationId(id);
                    List<CustomizationOptionTranslationDocument> optionTranslations = optionTranslationRepository
                            .findAllByCustomizationId(id);
                    return toI18nDto(document, translations, optionTranslations);
                });
    }

    @Override
    public Optional<CustomizationI18n> updateI18n(String id, CustomizationI18n customizationI18n) {
        Optional<CustomizationDocument> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        String defaultLang = i18NConfig.getDefaultLanguage();

        // Convert CustomizationOptionI18n list to CustomizationOptionDto list (default
        // language only)
        List<CustomizationOptionDto> options = customizationI18n.options() != null
                ? customizationI18n.options().stream()
                        .map(opt -> opt.toCustomizationOptionDto(defaultLang, defaultLang))
                        .toList()
                : new ArrayList<>();

        // Update default language values in main document
        CustomizationDocument document = new CustomizationDocument(
                id,
                customizationI18n.name().get(defaultLang, defaultLang),
                customizationI18n.type(),
                options,
                customizationI18n.usageCount());
        CustomizationDocument saved = repository.save(document);

        // Update translations for customization name
        updateTranslations(id, customizationI18n.name(), defaultLang);

        // Update option translations
        updateOptionTranslations(id, customizationI18n.options(), defaultLang);

        // Load all translations to return
        List<CustomizationTranslationDocument> translations = translationRepository.findAllByCustomizationId(id);
        List<CustomizationOptionTranslationDocument> optionTranslations = optionTranslationRepository
                .findAllByCustomizationId(id);
        return Optional.of(toI18nDto(saved, translations, optionTranslations));
    }

    @Override
    public Optional<CustomizationDto> findByIdInLocale(String id, String locale) {
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
    public List<CustomizationDto> findAllInLocale(String locale) {
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
    private CustomizationDto toDto(CustomizationDocument document) {
        // Convert options to include IDs
        List<CustomizationOptionDto> optionsWithIds = document.getOptions() != null
                ? IntStream.range(0, document.getOptions().size())
                        .mapToObj(i -> {
                            CustomizationOptionDto opt = document.getOptions().get(i);
                            return new CustomizationOptionDto(
                                    document.getId() + "-opt-" + i,
                                    opt.name(),
                                    opt.price(),
                                    opt.isSelectedByDefault(),
                                    opt.defaultValue());
                        })
                        .toList()
                : new ArrayList<>();

        return new CustomizationDto(
                document.getId(),
                document.getName(),
                document.getType(),
                optionsWithIds,
                document.getUsageCount());
    }

    /**
     * Convert document to DTO with translation fallback.
     * Implements field-level fallback for name and options.
     */
    private CustomizationDto toDtoWithTranslation(CustomizationDocument document, String requestedLang,
            String defaultLang) {
        // Try to load translation for customization name
        Optional<CustomizationTranslationDocument> translationOpt = translationRepository
                .findByCustomizationIdAndLanguage(
                        document.getId(), requestedLang);

        String translatedName = translationOpt
                .map(CustomizationTranslationDocument::getName)
                .filter(name -> name != null)
                .orElse(document.getName());

        // Apply translations to options with fallback and include IDs
        List<CustomizationOptionDto> translatedOptions = document.getOptions() != null
                ? IntStream.range(0, document.getOptions().size())
                        .mapToObj(i -> {
                            CustomizationOptionDto opt = document.getOptions().get(i);
                            String optionId = document.getId() + "-opt-" + i;

                            // Try to find translation for this option by option ID
                            Optional<CustomizationOptionTranslationDocument> optTranslationOpt = optionTranslationRepository
                                    .findByOptionIdAndLanguage(optionId, requestedLang);

                            String translatedOptionName = optTranslationOpt
                                    .map(CustomizationOptionTranslationDocument::getName)
                                    .filter(name -> name != null)
                                    .orElse(opt.name());

                            return new CustomizationOptionDto(
                                    optionId,
                                    translatedOptionName,
                                    opt.price(),
                                    opt.isSelectedByDefault(),
                                    opt.defaultValue());
                        })
                        .toList()
                : new ArrayList<>();

        return new CustomizationDto(
                document.getId(),
                translatedName,
                document.getType(),
                translatedOptions,
                document.getUsageCount());
    }

    /**
     * Field-level fallback helper.
     */
    private String fallback(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Convert document and translations to I18n DTO.
     */
    private CustomizationI18n toI18nDto(CustomizationDocument document,
            List<CustomizationTranslationDocument> translations,
            List<CustomizationOptionTranslationDocument> optionTranslations) {
        String defaultLang = i18NConfig.getDefaultLanguage();

        // Build name I18nField
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put(defaultLang, document.getName());
        for (CustomizationTranslationDocument translation : translations) {
            if (translation.getName() != null) {
                nameMap.put(translation.getLanguage(), translation.getName());
            }
        }
        I18nField name = new I18nField(nameMap);

        // Convert options to CustomizationOptionI18n
        List<CustomizationOptionI18n> i18nOptions = document.getOptions() != null
                ? IntStream.range(0, document.getOptions().size())
                        .mapToObj(i -> {
                            CustomizationOptionDto opt = document.getOptions().get(i);

                            // Build I18nField for this option's name
                            Map<String, String> optionNameMap = new HashMap<>();
                            optionNameMap.put(defaultLang, opt.name());

                            // Add translations for this option
                            for (CustomizationOptionTranslationDocument optTr : optionTranslations) {
                                if (optTr.getOptionIndex() == i && optTr.getName() != null) {
                                    optionNameMap.put(optTr.getLanguage(), optTr.getName());
                                }
                            }

                            return new CustomizationOptionI18n(
                                    document.getId() + "-opt-" + i,
                                    new I18nField(optionNameMap),
                                    opt.price(),
                                    opt.isSelectedByDefault(),
                                    opt.defaultValue());
                        })
                        .toList()
                : new ArrayList<>();

        return new CustomizationI18n(
                document.getId(),
                name,
                document.getType(),
                i18nOptions,
                document.getUsageCount());
    }

    /**
     * Update translations for customization name.
     */
    private void updateTranslations(String customizationId, I18nField nameField, String defaultLang) {
        // Delete existing translations
        translationRepository.deleteByCustomizationId(customizationId);

        // Create new translations for all non-default languages
        Map<String, String> nameTranslations = nameField.getAll();
        for (Map.Entry<String, String> entry : nameTranslations.entrySet()) {
            if (!entry.getKey().equals(defaultLang)) {
                CustomizationTranslationDocument translation = new CustomizationTranslationDocument(
                        customizationId,
                        entry.getKey(),
                        entry.getValue());
                translationRepository.save(translation);
            }
        }
    }

    /**
     * Update option translations.
     */
    private void updateOptionTranslations(String customizationId, List<CustomizationOptionI18n> options,
            String defaultLang) {
        // Delete existing option translations
        optionTranslationRepository.deleteByCustomizationId(customizationId);

        if (options == null) {
            return;
        }

        // Create new translations for all options
        for (int i = 0; i < options.size(); i++) {
            CustomizationOptionI18n option = options.get(i);
            String optionId = customizationId + "-opt-" + i;
            Map<String, String> optionNameTranslations = option.name().getAll();

            for (Map.Entry<String, String> entry : optionNameTranslations.entrySet()) {
                if (!entry.getKey().equals(defaultLang)) {
                    CustomizationOptionTranslationDocument translation = new CustomizationOptionTranslationDocument(
                            optionId,
                            customizationId,
                            i,
                            entry.getKey(),
                            entry.getValue());
                    optionTranslationRepository.save(translation);
                }
            }
        }
    }
}
