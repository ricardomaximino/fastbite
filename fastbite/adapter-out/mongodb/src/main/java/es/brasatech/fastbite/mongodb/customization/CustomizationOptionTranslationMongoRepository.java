package es.brasatech.fastbite.mongodb.customization;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for CustomizationOption translations.
 * Now supports efficient lookups by option ID.
 */
@Repository
@Profile("mongodb")
public interface CustomizationOptionTranslationMongoRepository
                extends MongoRepository<CustomizationOptionTranslationDocument, String> {

        /**
         * Find translation for a specific option by option ID and language.
         * This is the most efficient lookup for order translations.
         */
        Optional<CustomizationOptionTranslationDocument> findByOptionIdAndLanguage(
                        String optionId, String language);

        /**
         * Find all translations for a specific option (all languages).
         */
        List<CustomizationOptionTranslationDocument> findAllByOptionId(String optionId);

        /**
         * Find translation for a specific option and language (legacy method using
         * indices).
         */
        Optional<CustomizationOptionTranslationDocument> findByCustomizationIdAndOptionIndexAndLanguage(
                        String customizationId, int optionIndex, String language);

        /**
         * Find all option translations for a customization in a specific language.
         */
        List<CustomizationOptionTranslationDocument> findAllByCustomizationIdAndLanguage(
                        String customizationId, String language);

        /**
         * Find all translations for all options of a customization.
         */
        List<CustomizationOptionTranslationDocument> findAllByCustomizationId(String customizationId);

        /**
         * Delete all option translations for a specific customization.
         */
        void deleteByCustomizationId(String customizationId);

        /**
         * Delete all translations for a specific option.
         */
        void deleteByOptionId(String optionId);
}
