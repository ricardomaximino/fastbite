package es.brasatech.fastbite.jpa.group;

import es.brasatech.fastbite.application.office.GroupService;
import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.domain.group.Group;
import es.brasatech.fastbite.domain.group.GroupI18n;
import es.brasatech.fastbite.jpa.i18n.Translations;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JPA implementation of GroupService. Default-language texts live in the entity's columns,
 * other languages in its translations column.
 */
@Service
@Profile("jpa")
@RequiredArgsConstructor
@Transactional
public class GroupServiceJpaImpl implements GroupService {

    private final GroupJpaRepository repository;
    private final I18nConfig i18nConfig;

    @Override
    public List<Group> findAll() {
        return findAllInLocale(LocaleContextHolder.getLocale().getLanguage());
    }

    @Override
    public Optional<Group> findById(String id) {
        return findByIdInLocale(id, LocaleContextHolder.getLocale().getLanguage());
    }

    @Override
    public List<Group> findAllInLocale(String locale) {
        return repository.findAll().stream().map(entity -> toGroup(entity, locale)).toList();
    }

    @Override
    public Optional<Group> findByIdInLocale(String id, String locale) {
        return repository.findById(id).map(entity -> toGroup(entity, locale));
    }

    @Override
    public Group create(Group group) {
        GroupEntity entity = new GroupEntity(null, group.name(), group.description(), group.icon(), productsOf(group.products()));
        return toGroup(repository.save(entity), i18nConfig.getDefaultLanguage());
    }

    @Override
    public Optional<Group> update(String id, Group group) {
        return repository.findById(id).map(entity -> {
            entity.setName(group.name());
            entity.setDescription(group.description());
            entity.setIcon(group.icon());
            entity.setProducts(productsOf(group.products()));
            return toGroup(repository.save(entity), i18nConfig.getDefaultLanguage());
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
    public Optional<GroupI18n> findI18nById(String id) {
        return repository.findById(id).map(this::toGroupI18n);
    }

    @Override
    public Optional<GroupI18n> updateI18n(String id, GroupI18n groupI18n) {
        String defaultLang = i18nConfig.getDefaultLanguage();
        return repository.findById(id).map(entity -> {
            entity.setName(groupI18n.name().get(defaultLang, defaultLang));
            entity.setDescription(groupI18n.description().get(defaultLang, defaultLang));
            entity.setIcon(groupI18n.icon());
            entity.setProducts(productsOf(groupI18n.products()));
            entity.setTranslations(Translations.of(defaultLang,
                    Map.of("name", groupI18n.name(), "description", groupI18n.description())));
            return toGroupI18n(repository.save(entity));
        });
    }

    private Group toGroup(GroupEntity entity, String language) {
        var translations = entity.getTranslations();
        return new Group(
                entity.getId(),
                Translations.get(translations, language, "name", entity.getName()),
                Translations.get(translations, language, "description", entity.getDescription()),
                entity.getIcon(),
                entity.getProducts());
    }

    private GroupI18n toGroupI18n(GroupEntity entity) {
        String defaultLang = i18nConfig.getDefaultLanguage();
        var translations = entity.getTranslations();
        return new GroupI18n(
                entity.getId(),
                Translations.toI18nField(translations, "name", defaultLang, entity.getName()),
                Translations.toI18nField(translations, "description", defaultLang, entity.getDescription()),
                entity.getIcon(),
                entity.getProducts());
    }

    private static List<String> productsOf(List<String> products) {
        return products != null ? new ArrayList<>(products) : new ArrayList<>();
    }
}
