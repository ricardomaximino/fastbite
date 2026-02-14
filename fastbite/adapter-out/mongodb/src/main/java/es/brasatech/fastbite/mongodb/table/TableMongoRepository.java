package es.brasatech.fastbite.mongodb.table;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableMongoRepository extends MongoRepository<TableDocument, String> {
}
