package es.brasatech.fastbite.mongodb.group;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * MongoDB repository for Group documents.
 */
@Repository
@Profile("mongodb")
public interface GroupMongoRepository extends MongoRepository<GroupDocument, String> {
}
