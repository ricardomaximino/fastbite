package es.brasatech.fastbite.mongodb.product;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * MongoDB repository for Product documents.
 */
@Repository
@Profile("mongodb")
public interface ProductMongoRepository extends MongoRepository<ProductDocument, String> {
}
