package es.brasatech.fastbite.mongodb.group;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Group translations.
 */
@Repository
@Profile("mongodb")
public interface GroupTranslationMongoRepository extends MongoRepository<GroupTranslationDocument, String> {

    /**
     * Find translation for a specific group and language.
     */
    Optional<GroupTranslationDocument> findByGroupIdAndLanguage(String groupId, String language);

    /**
     * Find all translations for a specific group.
     */
    List<GroupTranslationDocument> findAllByGroupId(String groupId);

    /**
     * Delete all translations for a specific group.
     */
    void deleteByGroupId(String groupId);
}
