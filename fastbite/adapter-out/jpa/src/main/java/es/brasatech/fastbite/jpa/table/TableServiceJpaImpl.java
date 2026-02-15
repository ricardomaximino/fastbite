package es.brasatech.fastbite.jpa.table;

import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.table.Table;
import es.brasatech.fastbite.domain.table.TableI18n;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Profile("jpa")
@RequiredArgsConstructor
@Transactional
public class TableServiceJpaImpl implements TableService {
    private final TableJpaRepository repository;
    private final TableTranslationJpaRepository translationRepository;
    private final I18nConfig i18nConfig;

    @Override
    public List<Table> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Table> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Table create(Table table) {
        TableEntity entity = new TableEntity();
        if (table.id() != null && !table.id().isEmpty()) {
            entity.setId(table.id());
        }
        entity.setName(table.name());
        entity.setSeats(table.seats());
        entity.setStatus(table.status());
        entity.setActive(table.active());
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<Table> update(String id, Table table) {
        return repository.findById(id).map(entity -> {
            entity.setName(table.name());
            entity.setSeats(table.seats());
            entity.setStatus(table.status());
            entity.setActive(table.active());
            return toDomain(repository.save(entity));
        });
    }

    @Override
    public boolean delete(String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<TableI18n> findI18nById(String id) {
        return repository.findById(id).map(entity -> {
            List<TableTranslationEntity> translations = translationRepository.findAllByTableId(id);

            Map<String, String> nameMap = new HashMap<>();
            nameMap.put(i18nConfig.getDefaultLanguage(), entity.getName());
            for (TableTranslationEntity translation : translations) {
                if (translation.getName() != null) {
                    nameMap.put(translation.getLanguage(), translation.getName());
                }
            }

            return new TableI18n(entity.getId(), new I18nField(nameMap), entity.getSeats(), entity.getStatus(),
                    entity.isActive());
        });
    }

    @Override
    public void updateI18n(String id, TableI18n i18n) {
        repository.findById(id).ifPresent(entity -> {
            String defaultLang = i18nConfig.getDefaultLanguage();

            // Update main entity with default language value
            entity.setName(i18n.name().getDefault(defaultLang));
            entity.setSeats(i18n.seats());
            entity.setStatus(i18n.status());
            entity.setActive(i18n.active());
            repository.save(entity);

            // Update translations
            translationRepository.deleteByTableId(id);

            Map<String, String> nameTranslations = i18n.name().getAll();
            nameTranslations.forEach((lang, value) -> {
                if (!lang.equals(defaultLang) && value != null && !value.isEmpty()) {
                    translationRepository.save(new TableTranslationEntity(entity, lang, value));
                }
            });
        });
    }

    private Table toDomain(TableEntity entity) {
        return new Table(entity.getId(), entity.getName(), entity.getSeats(), entity.getStatus(), entity.isActive());
    }
}
