package es.brasatech.fastbite.jpa.customization;

import es.brasatech.fastbite.application.office.CustomizationService;
import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.customization.CustomizationDto;
import es.brasatech.fastbite.domain.customization.CustomizationI18n;
import es.brasatech.fastbite.domain.customization.CustomizationOptionDto;
import es.brasatech.fastbite.domain.customization.CustomizationOptionI18n;
import es.brasatech.fastbite.jpa.i18n.Translations;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of CustomizationService. Default-language texts live in the entities' columns,
 * other languages in their translations columns (customization and each option).
 */
@Service
@Profile("jpa")
@RequiredArgsConstructor
@Transactional
public class CustomizationServiceJpaImpl implements CustomizationService {

    private final CustomizationJpaRepository repository;
    private final I18nConfig i18nConfig;

    @Override
    public List<CustomizationDto> findAll() {
        return findAllInLocale(LocaleContextHolder.getLocale().getLanguage());
    }

    @Override
    public Optional<CustomizationDto> findById(String id) {
        return findByIdInLocale(id, LocaleContextHolder.getLocale().getLanguage());
    }

    @Override
    public List<CustomizationDto> findAllInLocale(String locale) {
        return repository.findAll().stream().map(entity -> toDto(entity, locale)).toList();
    }

    @Override
    public Optional<CustomizationDto> findByIdInLocale(String id, String locale) {
        return repository.findById(id).map(entity -> toDto(entity, locale));
    }

    @Override
    public CustomizationDto create(CustomizationDto customizationDto) {
        CustomizationEntity saved = repository.save(
                new CustomizationEntity(null, customizationDto.name(), customizationDto.type(), 0));
        if (customizationDto.options() != null && !customizationDto.options().isEmpty()) {
            replaceOptions(saved, customizationDto.options().stream().map(this::toOptionI18n).toList(), false);
            saved = repository.save(saved);
        }
        return toDto(saved, i18nConfig.getDefaultLanguage());
    }

    @Override
    public Optional<CustomizationDto> update(String id, CustomizationDto customizationDto) {
        return repository.findById(id).map(existing -> {
            existing.setName(customizationDto.name());
            existing.setType(customizationDto.type());
            List<CustomizationOptionI18n> options = customizationDto.options() != null
                    ? customizationDto.options().stream().map(this::toOptionI18n).toList()
                    : List.of();
            replaceOptions(existing, options, false);
            return toDto(repository.save(existing), i18nConfig.getDefaultLanguage());
        });
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

    @Override
    public Optional<CustomizationI18n> findI18nById(String id) {
        return repository.findById(id).map(this::toI18nDto);
    }

    @Override
    public Optional<CustomizationI18n> updateI18n(String id, CustomizationI18n customizationI18n) {
        String defaultLang = i18nConfig.getDefaultLanguage();
        return repository.findById(id).map(existing -> {
            existing.setName(customizationI18n.name().get(defaultLang, defaultLang));
            existing.setType(customizationI18n.type());
            existing.setUsageCount(customizationI18n.usageCount());
            existing.setTranslations(Translations.of(defaultLang, Map.of("name", customizationI18n.name())));
            replaceOptions(existing, customizationI18n.options() != null ? customizationI18n.options() : List.of(), true);
            return toI18nDto(repository.save(existing));
        });
    }

    /**
     * Recreates the options (ids "{customizationId}-opt-{index}"). With {@code useGivenTranslations} the option
     * translations come from the given names; otherwise each option keeps the translations it had under the same id.
     */
    private void replaceOptions(CustomizationEntity customization, List<CustomizationOptionI18n> options,
                                boolean useGivenTranslations) {
        String defaultLang = i18nConfig.getDefaultLanguage();
        Map<String, Map<String, Map<String, String>>> previousTranslations = customization.getOptions().stream()
                .collect(Collectors.toMap(CustomizationOptionEntity::getId, CustomizationOptionEntity::getTranslations));
        customization.clearOptions();
        for (int i = 0; i < options.size(); i++) {
            CustomizationOptionI18n option = options.get(i);
            String optionId = customization.getId() + "-opt-" + i;
            CustomizationOptionEntity entity = new CustomizationOptionEntity(
                    optionId,
                    option.name().get(defaultLang, defaultLang),
                    option.price(),
                    option.isSelectedByDefault(),
                    option.defaultValue(),
                    i);
            entity.setTranslations(useGivenTranslations
                    ? Translations.of(defaultLang, Map.of("name", option.name()))
                    : new HashMap<>(previousTranslations.getOrDefault(optionId, Map.of())));
            customization.addOption(entity);
        }
    }

    private CustomizationOptionI18n toOptionI18n(CustomizationOptionDto option) {
        return new CustomizationOptionI18n(
                option.id(),
                I18nField.of(i18nConfig.getDefaultLanguage(), option.name()),
                option.price(),
                option.isSelectedByDefault(),
                option.defaultValue());
    }

    private CustomizationDto toDto(CustomizationEntity entity, String language) {
        List<CustomizationOptionDto> options = entity.getOptions().stream()
                .map(option -> new CustomizationOptionDto(
                        option.getId(),
                        Translations.get(option.getTranslations(), language, "name", option.getName()),
                        option.getPrice(),
                        option.isSelectedByDefault(),
                        option.getDefaultValue()))
                .toList();
        return new CustomizationDto(
                entity.getId(),
                Translations.get(entity.getTranslations(), language, "name", entity.getName()),
                entity.getType(),
                options,
                entity.getUsageCount());
    }

    private CustomizationI18n toI18nDto(CustomizationEntity entity) {
        String defaultLang = i18nConfig.getDefaultLanguage();
        List<CustomizationOptionI18n> options = entity.getOptions().stream()
                .map(option -> new CustomizationOptionI18n(
                        option.getId(),
                        Translations.toI18nField(option.getTranslations(), "name", defaultLang, option.getName()),
                        option.getPrice(),
                        option.isSelectedByDefault(),
                        option.getDefaultValue()))
                .toList();
        return new CustomizationI18n(
                entity.getId(),
                Translations.toI18nField(entity.getTranslations(), "name", defaultLang, entity.getName()),
                entity.getType(),
                options,
                entity.getUsageCount());
    }
}
