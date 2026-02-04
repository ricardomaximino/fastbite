package es.brasatech.fastbite.application.office;

import es.brasatech.fastbite.domain.customization.CustomizationDto;
import es.brasatech.fastbite.domain.customization.CustomizationI18n;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing customizations in the BackOffice system.
 * Implementations can use different persistence strategies (in-memory, MongoDB,
 * JPA).
 */
public interface CustomizationService {

    /**
     * Get all customizations
     */
    List<CustomizationDto> findAll();

    /**
     * Find customization by ID
     */
    Optional<CustomizationDto> findById(String id);

    /**
     * Create a new customization
     */
    CustomizationDto create(CustomizationDto customizationDto);

    /**
     * Update an existing customization
     */
    Optional<CustomizationDto> update(String id, CustomizationDto customizationDto);

    /**
     * Delete a customization
     */
    boolean delete(String id);

    /**
     * Clear all customizations (for testing)
     */
    void clear();

    // ===== I18n Methods =====

    /**
     * Get customization with i18n data (all translations)
     */
    Optional<CustomizationI18n> findI18nById(String id);

    /**
     * Update customization translations
     */
    Optional<CustomizationI18n> updateI18n(String id, CustomizationI18n customizationI18n);

    /**
     * Get customization in specific locale with fallback to default
     */
    Optional<CustomizationDto> findByIdInLocale(String id, String locale);

    /**
     * Get all customizations in specific locale with fallback to default
     */
    List<CustomizationDto> findAllInLocale(String locale);
}
