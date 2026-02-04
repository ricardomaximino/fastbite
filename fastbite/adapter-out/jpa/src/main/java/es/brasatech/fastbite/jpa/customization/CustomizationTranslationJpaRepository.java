package es.brasatech.fastbite.jpa.customization;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CustomizationTranslation entities.
 */
@Repository
@Profile("jpa")
public interface CustomizationTranslationJpaRepository extends JpaRepository<CustomizationTranslationEntity, String> {

    /**
     * Find translation for a specific customization and language.
     */
    @Query("""
            SELECT ct
            FROM CustomizationTranslation ct
            WHERE ct.customization.id = :customizationId
            AND ct.language = :language
            """)
    Optional<CustomizationTranslationEntity> findByCustomizationIdAndLanguage(
            @Param("customizationId") String customizationId,
            @Param("language") String language);

    /**
     * Find all translations for a specific customization.
     */
    @Query("""
            SELECT ct
            FROM CustomizationTranslation ct
            WHERE ct.customization.id = :customizationId
            """)
    List<CustomizationTranslationEntity> findAllByCustomizationId(@Param("customizationId") String customizationId);

    /**
     * Delete all translations for a specific customization.
     */
    @Modifying
    @Query("DELETE FROM CustomizationTranslation ct WHERE ct.customization.id = :customizationId")
    void deleteByCustomizationId(@Param("customizationId") String customizationId);
}
