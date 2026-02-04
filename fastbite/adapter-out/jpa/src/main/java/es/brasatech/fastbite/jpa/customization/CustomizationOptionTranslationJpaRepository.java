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
 * Repository for CustomizationOptionTranslation entities.
 * Now uses direct reference to CustomizationOptionEntity for efficient lookups.
 */
@Repository
@Profile("jpa")
public interface CustomizationOptionTranslationJpaRepository
        extends JpaRepository<CustomizationOptionTranslationEntity, String> {

    /**
     * Find translation for a specific customization option and language.
     * Uses option ID directly for efficient lookup.
     */
    @Query("""
            SELECT cot
            FROM CustomizationOptionTranslation cot
            WHERE cot.customizationOption.id = :optionId
            AND cot.language = :language
            """)
    Optional<CustomizationOptionTranslationEntity> findByOptionIdAndLanguage(
            @Param("optionId") String optionId,
            @Param("language") String language);

    /**
     * Find all translations for all options of a customization in a specific
     * language.
     */
    @Query("""
            SELECT cot
            FROM CustomizationOptionTranslation cot
            WHERE cot.customizationOption.customization.id = :customizationId
            AND cot.language = :language
            """)
    List<CustomizationOptionTranslationEntity> findAllByCustomizationIdAndLanguage(
            @Param("customizationId") String customizationId,
            @Param("language") String language);

    /**
     * Find all translations for a specific customization.
     */
    @Query("""
            SELECT cot
            FROM CustomizationOptionTranslation cot
            WHERE cot.customizationOption.customization.id = :customizationId
            """)
    List<CustomizationOptionTranslationEntity> findAllByCustomizationId(
            @Param("customizationId") String customizationId);

    /**
     * Find all translations for a specific option (all languages).
     */
    @Query("""
            SELECT cot
            FROM CustomizationOptionTranslation cot
            WHERE cot.customizationOption.id = :optionId
            """)
    List<CustomizationOptionTranslationEntity> findAllByOptionId(@Param("optionId") String optionId);

    /**
     * Delete all translations for a specific customization.
     */
    @Modifying
    @Query("DELETE FROM CustomizationOptionTranslation cot WHERE cot.customizationOption.customization.id = :customizationId")
    void deleteByCustomizationId(@Param("customizationId") String customizationId);

    /**
     * Delete all translations for a specific option.
     */
    @Modifying
    @Query("DELETE FROM CustomizationOptionTranslation cot WHERE cot.customizationOption.id = :optionId")
    void deleteByOptionId(@Param("optionId") String optionId);
}
