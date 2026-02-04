package es.brasatech.fastbite.jpa.customization;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * JPA implementation of CustomizationService for relational databases
 * (PostgreSQL, MySQL).
 * Uses separate translation tables for non-default languages.
 * Active when profile 'jpa' is enabled.
 */
@Service
@Profile("jpa")
@RequiredArgsConstructor
@Transactional
public class CustomizationServiceJpaImpl implements CustomizationService {

    private final CustomizationJpaRepository repository;
    private final CustomizationTranslationJpaRepository translationRepository;
    private final CustomizationOptionJpaRepository optionRepository;
    private final CustomizationOptionTranslationJpaRepository optionTranslationRepository;
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

        // Load entities and apply translations
        return repository.findAll().stream()
                .map(entity -> toDtoWithTranslation(entity, requestedLang, defaultLang))
                .toList();
    }

    @Override
    public Optional<CustomizationDto> findById(String id) {
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
    public CustomizationDto create(CustomizationDto customizationDto) {
        // Create CustomizationEntity first (without options)
        CustomizationEntity entity = new CustomizationEntity(
                null, // JPA will generate ID
                customizationDto.name(),
                customizationDto.type(),
                0 // Initial usage count
        );
        CustomizationEntity saved = repository.save(entity);

        // Create and add option entities with proper IDs
        if (customizationDto.options() != null && !customizationDto.options().isEmpty()) {
            for (int i = 0; i < customizationDto.options().size(); i++) {
                CustomizationOptionDto optDto = customizationDto.options().get(i);

                CustomizationOptionEntity optionEntity = new CustomizationOptionEntity(
                        saved.getId() + "-opt-" + i, // Generate ID
                        optDto.name(),
                        optDto.price(),
                        optDto.isSelectedByDefault(),
                        optDto.defaultValue(),
                        i // optionIndex
                );

                // Add option and maintain bidirectional relationship
                saved.addOption(optionEntity);
            }

            // Save again to persist options (cascade will handle it)
            saved = repository.save(saved);
        }

        return toDto(saved);
    }

    @Override
    public Optional<CustomizationDto> update(String id, CustomizationDto customizationDto) {
        Optional<CustomizationEntity> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        CustomizationEntity existing = existingOpt.get();

        // Update basic properties
        existing.setName(customizationDto.name());
        existing.setType(customizationDto.type());

        // Clear existing options
        existing.clearOptions();

        // Add new options with proper IDs
        if (customizationDto.options() != null && !customizationDto.options().isEmpty()) {
            for (int i = 0; i < customizationDto.options().size(); i++) {
                CustomizationOptionDto optDto = customizationDto.options().get(i);

                CustomizationOptionEntity optionEntity = new CustomizationOptionEntity(
                        id + "-opt-" + i, // Generate ID
                        optDto.name(),
                        optDto.price(),
                        optDto.isSelectedByDefault(),
                        optDto.defaultValue(),
                        i // optionIndex
                );

                // Add option and maintain bidirectional relationship
                existing.addOption(optionEntity);
            }
        }

        // Save (cascade will handle options)
        CustomizationEntity saved = repository.save(existing);
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
                .map(entity -> {
                    // Load all translations for this customization
                    List<CustomizationTranslationEntity> translations = translationRepository
                            .findAllByCustomizationId(id);
                    List<CustomizationOptionTranslationEntity> optionTranslations = optionTranslationRepository
                            .findAllByCustomizationId(id);
                    return toI18nDto(entity, translations, optionTranslations);
                });
    }

    @Override
    public Optional<CustomizationI18n> updateI18n(String id, CustomizationI18n customizationI18n) {
        Optional<CustomizationEntity> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        CustomizationEntity existing = existingOpt.get();
        String defaultLang = i18NConfig.getDefaultLanguage();

        // Update basic properties with default language values
        existing.setName(customizationI18n.name().get(defaultLang, defaultLang));
        existing.setType(customizationI18n.type());
        existing.setUsageCount(customizationI18n.usageCount());

        // Clear existing options
        existing.clearOptions();

        // Add new options with proper IDs and default language values
        if (customizationI18n.options() != null && !customizationI18n.options().isEmpty()) {
            for (int i = 0; i < customizationI18n.options().size(); i++) {
                CustomizationOptionI18n optI18n = customizationI18n.options().get(i);

                CustomizationOptionEntity optionEntity = new CustomizationOptionEntity(
                        id + "-opt-" + i, // Generate ID
                        optI18n.name().get(defaultLang, defaultLang), // Default language name
                        optI18n.price(),
                        optI18n.isSelectedByDefault(),
                        optI18n.defaultValue(),
                        i // optionIndex
                );

                // Add option and maintain bidirectional relationship
                existing.addOption(optionEntity);
            }
        }

        // Save (cascade will handle options)
        CustomizationEntity saved = repository.save(existing);

        // Update translations for customization name
        updateTranslations(id, customizationI18n.name(), defaultLang);

        // Update option translations
        updateOptionTranslations(id, customizationI18n.options(), defaultLang);

        // Load all translations to return
        List<CustomizationTranslationEntity> translations = translationRepository.findAllByCustomizationId(id);
        List<CustomizationOptionTranslationEntity> optionTranslations = optionTranslationRepository
                .findAllByCustomizationId(id);
        return Optional.of(toI18nDto(saved, translations, optionTranslations));
    }

    @Override
    public Optional<CustomizationDto> findByIdInLocale(String id, String locale) {
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
    public List<CustomizationDto> findAllInLocale(String locale) {
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
    private CustomizationDto toDto(CustomizationEntity entity) {
        // Convert CustomizationOptionEntity to CustomizationOptionDto
        List<CustomizationOptionDto> optionDtos = entity.getOptions() != null
                ? entity.getOptions().stream()
                        .map(optEntity -> new CustomizationOptionDto(
                                optEntity.getId(),
                                optEntity.getName(),
                                optEntity.getPrice(),
                                optEntity.isSelectedByDefault(),
                                optEntity.getDefaultValue()))
                        .toList()
                : new ArrayList<>();

        return new CustomizationDto(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                optionDtos,
                entity.getUsageCount());
    }

    /**
     * Convert entity to DTO with translation fallback.
     * Implements field-level fallback for name and options.
     */
    private CustomizationDto toDtoWithTranslation(CustomizationEntity entity, String requestedLang,
            String defaultLang) {
        // Try to load translation for customization name
        Optional<CustomizationTranslationEntity> translationOpt = translationRepository
                .findByCustomizationIdAndLanguage(
                        entity.getId(), requestedLang);

        String translatedName = translationOpt
                .map(CustomizationTranslationEntity::getName)
                .filter(name -> name != null)
                .orElse(entity.getName());

        // Apply translations to options with fallback
        List<CustomizationOptionDto> translatedOptions = entity.getOptions() != null
                ? entity.getOptions().stream()
                        .map(optEntity -> {
                            // Try to find translation for this option
                            Optional<CustomizationOptionTranslationEntity> optTranslationOpt = optionTranslationRepository
                                    .findByOptionIdAndLanguage(optEntity.getId(), requestedLang);

                            String translatedOptionName = optTranslationOpt
                                    .map(CustomizationOptionTranslationEntity::getName)
                                    .filter(name -> name != null)
                                    .orElse(optEntity.getName());

                            return new CustomizationOptionDto(
                                    optEntity.getId(),
                                    translatedOptionName,
                                    optEntity.getPrice(),
                                    optEntity.isSelectedByDefault(),
                                    optEntity.getDefaultValue());
                        })
                        .toList()
                : new ArrayList<>();

        return new CustomizationDto(
                entity.getId(),
                translatedName,
                entity.getType(),
                translatedOptions,
                entity.getUsageCount());
    }

    /**
     * Convert entity and translations to I18n DTO.
     */
    private CustomizationI18n toI18nDto(CustomizationEntity entity,
            List<CustomizationTranslationEntity> translations,
            List<CustomizationOptionTranslationEntity> optionTranslations) {
        String defaultLang = i18NConfig.getDefaultLanguage();

        // Build name I18nField
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put(defaultLang, entity.getName());
        for (CustomizationTranslationEntity translation : translations) {
            if (translation.getName() != null) {
                nameMap.put(translation.getLanguage(), translation.getName());
            }
        }
        I18nField name = new I18nField(nameMap);

        // Convert options to CustomizationOptionI18n
        List<CustomizationOptionI18n> i18nOptions = entity.getOptions() != null
                ? entity.getOptions().stream()
                        .map(optEntity -> {
                            // Build I18nField for this option's name
                            Map<String, String> optionNameMap = new HashMap<>();
                            optionNameMap.put(defaultLang, optEntity.getName());

                            // Add translations for this option using option ID
                            for (CustomizationOptionTranslationEntity optTr : optionTranslations) {
                                if (optTr.getCustomizationOption().getId().equals(optEntity.getId())
                                        && optTr.getName() != null) {
                                    optionNameMap.put(optTr.getLanguage(), optTr.getName());
                                }
                            }

                            return new CustomizationOptionI18n(
                                    optEntity.getId(),
                                    new I18nField(optionNameMap),
                                    optEntity.getPrice(),
                                    optEntity.isSelectedByDefault(),
                                    optEntity.getDefaultValue());
                        })
                        .toList()
                : new ArrayList<>();

        return new CustomizationI18n(
                entity.getId(),
                name,
                entity.getType(),
                i18nOptions,
                entity.getUsageCount());
    }

    /**
     * Update translations for customization name.
     */
    private void updateTranslations(String customizationId, I18nField nameField, String defaultLang) {
        // Delete existing translations
        translationRepository.deleteByCustomizationId(customizationId);

        // Get the customization entity for reference
        CustomizationEntity customization = repository.findById(customizationId).orElseThrow();

        // Create new translations for all non-default languages
        Map<String, String> nameTranslations = nameField.getAll();
        for (Map.Entry<String, String> entry : nameTranslations.entrySet()) {
            if (!entry.getKey().equals(defaultLang)) {
                CustomizationTranslationEntity translation = new CustomizationTranslationEntity(
                        customization,
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

        // Get the customization entity to access its options
        CustomizationEntity customization = repository.findById(customizationId).orElseThrow();
        List<CustomizationOptionEntity> optionEntities = customization.getOptions();

        // Create new translations for all options
        for (int i = 0; i < options.size(); i++) {
            if (i >= optionEntities.size()) {
                break; // Safety check
            }

            CustomizationOptionI18n optionI18n = options.get(i);
            CustomizationOptionEntity optionEntity = optionEntities.get(i);
            Map<String, String> optionNameTranslations = optionI18n.name().getAll();

            for (Map.Entry<String, String> entry : optionNameTranslations.entrySet()) {
                if (!entry.getKey().equals(defaultLang)) {
                    CustomizationOptionTranslationEntity translation = new CustomizationOptionTranslationEntity(
                            optionEntity, // Reference the actual option entity
                            entry.getKey(),
                            entry.getValue());
                    optionTranslationRepository.save(translation);
                }
            }
        }
    }
}
