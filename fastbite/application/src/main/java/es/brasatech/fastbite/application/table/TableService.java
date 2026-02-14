package es.brasatech.fastbite.application.table;

import es.brasatech.fastbite.domain.table.Table;
import es.brasatech.fastbite.domain.table.TableI18n;

import java.util.List;
import java.util.Optional;

public interface TableService {
    List<Table> findAll();

    Optional<Table> findById(String id);

    Table create(Table table);

    Optional<Table> update(String id, Table table);

    boolean delete(String id);

    Optional<TableI18n> findI18nById(String id);

    void updateI18n(String id, TableI18n i18n);
}
