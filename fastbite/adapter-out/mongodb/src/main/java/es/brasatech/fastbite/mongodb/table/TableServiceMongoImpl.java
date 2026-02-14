package es.brasatech.fastbite.mongodb.table;

import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.table.Table;
import es.brasatech.fastbite.domain.table.TableI18n;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Profile("mongodb")
@RequiredArgsConstructor
public class TableServiceMongoImpl implements TableService {
    private final TableMongoRepository repository;
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
        TableDocument document = new TableDocument();
        document.setId(table.id());
        document.setName(table.name());
        document.setCapacity(table.capacity());
        document.setActive(table.active());
        return toDomain(repository.save(document));
    }

    @Override
    public Optional<Table> update(String id, Table table) {
        return repository.findById(id).map(document -> {
            document.setName(table.name());
            document.setCapacity(table.capacity());
            document.setActive(table.active());
            return toDomain(repository.save(document));
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
        return repository.findById(id).map(document -> {
            I18nField name = new I18nField();
            name.set(i18nConfig.getDefaultLanguage(), document.getName());
            return new TableI18n(document.getId(), name, document.getCapacity(), document.isActive());
        });
    }

    @Override
    public void updateI18n(String id, TableI18n i18n) {
        repository.findById(id).ifPresent(document -> {
            document.setName(i18n.name().getDefault(i18nConfig.getDefaultLanguage()));
            document.setCapacity(i18n.capacity());
            document.setActive(i18n.active());
            repository.save(document);
        });
    }

    private Table toDomain(TableDocument document) {
        return new Table(document.getId(), document.getName(), document.getCapacity(), document.isActive());
    }
}
