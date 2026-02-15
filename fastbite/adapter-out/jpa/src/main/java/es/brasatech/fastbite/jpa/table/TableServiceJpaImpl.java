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

import java.util.List;
import java.util.Optional;

@Service
@Profile("jpa")
@RequiredArgsConstructor
@Transactional
public class TableServiceJpaImpl implements TableService {
    private final TableJpaRepository repository;
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
        entity.setId(table.id());
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
        // Simple implementation for now, assuming name is stored in default language
        return repository.findById(id).map(entity -> {
            I18nField name = new I18nField();
            name.set(i18nConfig.getDefaultLanguage(), entity.getName());
            return new TableI18n(entity.getId(), name, entity.getSeats(), entity.getStatus(), entity.isActive());
        });
    }

    @Override
    public void updateI18n(String id, TableI18n i18n) {
        repository.findById(id).ifPresent(entity -> {
            entity.setName(i18n.name().getDefault(i18nConfig.getDefaultLanguage()));
            entity.setSeats(i18n.seats());
            entity.setStatus(i18n.status());
            entity.setActive(i18n.active());
            repository.save(entity);
        });
    }

    private Table toDomain(TableEntity entity) {
        return new Table(entity.getId(), entity.getName(), entity.getSeats(), entity.getStatus(), entity.isActive());
    }
}
