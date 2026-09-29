package es.brasatech.fastbite.application.office;

import es.brasatech.fastbite.domain.customization.CustomizationDto;

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
}
