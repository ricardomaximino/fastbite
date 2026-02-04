package es.brasatech.fastbite.application.office;

import es.brasatech.fastbite.domain.product.ProductDto;
import es.brasatech.fastbite.domain.product.ProductI18n;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing products in the BackOffice system.
 * Implementations can use different persistence strategies (in-memory, MongoDB,
 * JPA).
 */
public interface ProductService {

    /**
     * Get all products
     */
    List<ProductDto> findAll();

    /**
     * Find product by ID
     */
    Optional<ProductDto> findById(String id);

    /**
     * Create a new product
     */
    ProductDto create(ProductDto productDto);

    /**
     * Update an existing product
     */
    Optional<ProductDto> update(String id, ProductDto productDto);

    /**
     * Delete a product
     */
    boolean delete(String id);

    /**
     * Clear all products (for testing)
     */
    void clear();

    // ===== I18n Methods =====

    /**
     * Get product with i18n data (all translations)
     */
    Optional<ProductI18n> findI18nById(String id);

    /**
     * Update product translations
     */
    Optional<ProductI18n> updateI18n(String id, ProductI18n productI18n);

    /**
     * Get product in specific locale with fallback to default
     */
    Optional<ProductDto> findByIdInLocale(String id, String locale);

    /**
     * Get all products in specific locale with fallback to default
     */
    List<ProductDto> findAllInLocale(String locale);
}
