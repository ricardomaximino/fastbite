package es.brasatech.fastbite.mongodb.customization;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Customization translations.
 */
@Repository
@Profile("mongodb")
public interface CustomizationTranslationMongoRepository
        extends MongoRepository<CustomizationTranslationDocument, String> {

    /**
     * Find translation for a specific customization and language.
     */
    Optional<CustomizationTranslationDocument> findByCustomizationIdAndLanguage(String customizationId,
            String language);

    /**
     * Find all translations for a specific customization.
     */
    List<CustomizationTranslationDocument> findAllByCustomizationId(String customizationId);

    /**
     * Delete all translations for a specific customization.
     */
    void deleteByCustomizationId(String customizationId);
}
