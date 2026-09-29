package es.brasatech.fastbite.domain.customization;

import java.util.List;

/**
 * DTO for Customization management in BackOffice.
 */
public record CustomizationDto(
                String id,
                String name,
                String type, // radio, checkbox, quantity
                List<CustomizationOptionDto> options,
                int usageCount) {
}
