package es.brasatech.fastbite.mongodb.table;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface TableTranslationMongoRepository extends MongoRepository<TableTranslationDocument, String> {
    List<TableTranslationDocument> findAllByTableId(String tableId);

    Optional<TableTranslationDocument> findByTableIdAndLanguage(String tableId, String language);

    void deleteByTableId(String tableId);
}
