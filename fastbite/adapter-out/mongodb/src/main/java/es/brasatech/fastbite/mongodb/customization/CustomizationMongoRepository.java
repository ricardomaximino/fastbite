package es.brasatech.fastbite.mongodb.customization;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * MongoDB repository for Customization documents.
 */
@Repository
@Profile("mongodb")
public interface CustomizationMongoRepository extends MongoRepository<CustomizationDocument, String> {
}
