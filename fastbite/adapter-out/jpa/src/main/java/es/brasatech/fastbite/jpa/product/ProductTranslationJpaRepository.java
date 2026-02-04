package es.brasatech.fastbite.jpa.product;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductTranslation entities.
 */
@Repository
@Profile("jpa")
public interface ProductTranslationJpaRepository extends JpaRepository<ProductTranslationEntity, String> {

    /**
     * Find translation for a specific product and language.
     */
    @Query("""
            SELECT pt
            FROM ProductTranslation pt
            WHERE pt.product.id = :productId
            AND pt.language = :language
            """)
    Optional<ProductTranslationEntity> findByProductIdAndLanguage(
            @Param("productId") String productId,
            @Param("language") String language);

    /**
     * Find all translations for a specific product.
     */
    @Query("""
            SELECT pt
            FROM ProductTranslation pt
            WHERE pt.product.id = :productId
            """)
    List<ProductTranslationEntity> findAllByProductId(@Param("productId") String productId);

    /**
     * Delete all translations for a specific product.
     */
    @Modifying
    @Query("DELETE FROM ProductTranslation pt WHERE pt.product.id = :productId")
    void deleteByProductId(@Param("productId") String productId);
}
