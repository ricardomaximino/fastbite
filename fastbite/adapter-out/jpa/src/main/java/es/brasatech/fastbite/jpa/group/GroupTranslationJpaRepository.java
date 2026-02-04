package es.brasatech.fastbite.jpa.group;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for GroupTranslation entities.
 */
@Repository
@Profile("jpa")
public interface GroupTranslationJpaRepository extends JpaRepository<GroupTranslationEntity, String> {

    /**
     * Find translation for a specific group and language.
     */
    @Query("""
            SELECT gt
            FROM GroupTranslation gt
            WHERE gt.group.id = :groupId
            AND gt.language = :language
            """)
    Optional<GroupTranslationEntity> findByGroupIdAndLanguage(
            @Param("groupId") String groupId,
            @Param("language") String language);

    /**
     * Find all translations for a specific group.
     */
    @Query("""
            SELECT gt
            FROM GroupTranslation gt
            WHERE gt.group.id = :groupId
            """)
    List<GroupTranslationEntity> findAllByGroupId(@Param("groupId") String groupId);

    /**
     * Delete all translations for a specific group.
     */
    @Modifying
    @Query("DELETE FROM GroupTranslation gt WHERE gt.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") String groupId);
}
