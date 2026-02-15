package es.brasatech.fastbite.jpa.table;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TableTranslationJpaRepository extends JpaRepository<TableTranslationEntity, String> {
    List<TableTranslationEntity> findAllByTableId(String tableId);

    Optional<TableTranslationEntity> findByTableIdAndLanguage(String tableId, String language);

    void deleteByTableId(String tableId);
}
