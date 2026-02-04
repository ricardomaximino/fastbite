package es.brasatech.fastbite.mongodb.group;

import es.brasatech.fastbite.application.office.GroupService;
import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.group.Group;
import es.brasatech.fastbite.domain.group.GroupI18n;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * MongoDB implementation of GroupService.
 * Uses separate translation collection for non-default languages.
 * Active when profile 'mongodb' is enabled.
 */
@Service
@Profile("mongodb")
@RequiredArgsConstructor
public class GroupServiceMongoImpl implements GroupService {

    private final GroupMongoRepository repository;
    private final GroupTranslationMongoRepository translationRepository;
    private final I18nConfig i18NConfig;

    @Override
    public List<Group> findAll() {
        Locale locale = LocaleContextHolder.getLocale();
        String requestedLang = locale.getLanguage();
        String defaultLang = i18NConfig.getDefaultLanguage();

        // Fast path: if requested == default, no translation needed
        if (requestedLang.equals(defaultLang)) {
            return repository.findAll().stream()
                    .map(this::toGroup)
                    .toList();
        }

        // Load documents and apply translations
        return repository.findAll().stream()
                .map(document -> toGroupWithTranslation(document, requestedLang, defaultLang))
                .toList();
    }

    @Override
    public Optional<Group> findById(String id) {
        Locale locale = LocaleContextHolder.getLocale();
        String requestedLang = locale.getLanguage();
        String defaultLang = i18NConfig.getDefaultLanguage();

        return repository.findById(id)
                .map(document -> {
                    // Fast path: if requested == default, no translation needed
                    if (requestedLang.equals(defaultLang)) {
                        return toGroup(document);
                    }
                    // Apply translation with fallback
                    return toGroupWithTranslation(document, requestedLang, defaultLang);
                });
    }

    @Override
    public Group create(Group group) {
        GroupDocument document = new GroupDocument(
                null, // MongoDB will generate ID
                group.name(),
                group.description(),
                group.icon(),
                group.products() != null ? new ArrayList<>(group.products()) : new ArrayList<>());
        GroupDocument saved = repository.save(document);
        return toGroup(saved);
    }

    @Override
    public Optional<Group> update(String id, Group group) {
        Optional<GroupDocument> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        // Update default language values directly
        GroupDocument document = new GroupDocument(
                id,
                group.name(),
                group.description(),
                group.icon(),
                group.products() != null ? new ArrayList<>(group.products()) : new ArrayList<>());
        GroupDocument saved = repository.save(document);
        return Optional.of(toGroup(saved));
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
    public Optional<GroupI18n> findI18nById(String id) {
        return repository.findById(id)
                .map(document -> {
                    // Load all translations for this group
                    List<GroupTranslationDocument> translations = translationRepository.findAllByGroupId(id);
                    return toGroupI18n(document, translations);
                });
    }

    @Override
    public Optional<GroupI18n> updateI18n(String id, GroupI18n groupI18n) {
        Optional<GroupDocument> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        String defaultLang = i18NConfig.getDefaultLanguage();

        // Update default language values in main document
        GroupDocument document = new GroupDocument(
                id,
                groupI18n.name().get(defaultLang, defaultLang),
                groupI18n.description().get(defaultLang, defaultLang),
                groupI18n.icon(),
                groupI18n.products() != null ? new ArrayList<>(groupI18n.products()) : new ArrayList<>());
        GroupDocument saved = repository.save(document);

        // Update translations for non-default languages
        updateTranslations(id, groupI18n.name(), groupI18n.description(), defaultLang);

        // Load all translations to return
        List<GroupTranslationDocument> translations = translationRepository.findAllByGroupId(id);
        return Optional.of(toGroupI18n(saved, translations));
    }

    @Override
    public Optional<Group> findByIdInLocale(String id, String locale) {
        String defaultLang = i18NConfig.getDefaultLanguage();
        return repository.findById(id)
                .map(document -> {
                    // Fast path: if requested == default, no translation needed
                    if (locale.equals(defaultLang)) {
                        return toGroup(document);
                    }
                    // Apply translation with fallback
                    return toGroupWithTranslation(document, locale, defaultLang);
                });
    }

    @Override
    public List<Group> findAllInLocale(String locale) {
        String defaultLang = i18NConfig.getDefaultLanguage();

        // Fast path: if requested == default, no translation needed
        if (locale.equals(defaultLang)) {
            return repository.findAll().stream()
                    .map(this::toGroup)
                    .toList();
        }

        // Load documents and apply translations
        return repository.findAll().stream()
                .map(document -> toGroupWithTranslation(document, locale, defaultLang))
                .toList();
    }

    // ===== Converter Methods =====

    /**
     * Convert document to Group using default language values (fast path).
     */
    private Group toGroup(GroupDocument document) {
        return new Group(
                document.getId(),
                document.getName(),
                document.getDescription(),
                document.getIcon(),
                document.getProducts());
    }

    /**
     * Convert document to Group with translation fallback.
     * Implements field-level fallback: translation field → default field → full
     * fallback.
     */
    private Group toGroupWithTranslation(GroupDocument document, String requestedLang, String defaultLang) {
        // Try to load translation
        Optional<GroupTranslationDocument> translationOpt = translationRepository.findByGroupIdAndLanguage(
                document.getId(), requestedLang);

        if (translationOpt.isPresent()) {
            GroupTranslationDocument translation = translationOpt.get();
            // Field-level fallback: use translation if present, otherwise fallback to
            // default
            return new Group(
                    document.getId(),
                    fallback(translation.getName(), document.getName()),
                    fallback(translation.getDescription(), document.getDescription()),
                    document.getIcon(),
                    document.getProducts());
        }

        // Full fallback: no translation found, use default language values
        return toGroup(document);
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
    private GroupI18n toGroupI18n(GroupDocument document, List<GroupTranslationDocument> translations) {
        String defaultLang = i18NConfig.getDefaultLanguage();

        // Build name I18nField (default language + translations)
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put(defaultLang, document.getName());
        for (GroupTranslationDocument translation : translations) {
            if (translation.getName() != null) {
                nameMap.put(translation.getLanguage(), translation.getName());
            }
        }
        I18nField name = new I18nField(nameMap);

        // Build description I18nField (default language + translations)
        Map<String, String> descriptionMap = new HashMap<>();
        descriptionMap.put(defaultLang, document.getDescription());
        for (GroupTranslationDocument translation : translations) {
            if (translation.getDescription() != null) {
                descriptionMap.put(translation.getLanguage(), translation.getDescription());
            }
        }
        I18nField description = new I18nField(descriptionMap);

        return new GroupI18n(
                document.getId(),
                name,
                description,
                document.getIcon(),
                document.getProducts());
    }

    /**
     * Update translations for non-default languages.
     * Deletes old translations and creates new ones based on I18nField contents.
     */
    private void updateTranslations(String groupId, I18nField nameField, I18nField descriptionField,
            String defaultLang) {
        // Delete existing translations
        translationRepository.deleteByGroupId(groupId);

        // Create new translations for all non-default languages
        Map<String, String> nameTranslations = nameField.getAll();
        Map<String, String> descriptionTranslations = descriptionField.getAll();

        // Collect all languages (from both name and description)
        var allLanguages = new java.util.HashSet<String>();
        allLanguages.addAll(nameTranslations.keySet());
        allLanguages.addAll(descriptionTranslations.keySet());
        allLanguages.remove(defaultLang); // Don't create translation for default language

        for (String lang : allLanguages) {
            GroupTranslationDocument translation = new GroupTranslationDocument(
                    groupId,
                    lang,
                    nameTranslations.get(lang),
                    descriptionTranslations.get(lang));
            translationRepository.save(translation);
        }
    }
}
