package es.brasatech.fastbite.mongodb.product;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Product translations.
 */
@Repository
@Profile("mongodb")
public interface ProductTranslationMongoRepository extends MongoRepository<ProductTranslationDocument, String> {

    /**
     * Find translation for a specific product and language.
     */
    Optional<ProductTranslationDocument> findByProductIdAndLanguage(String productId, String language);

    /**
     * Find all translations for a specific product.
     */
    List<ProductTranslationDocument> findAllByProductId(String productId);

    /**
     * Delete all translations for a specific product.
     */
    void deleteByProductId(String productId);
}
