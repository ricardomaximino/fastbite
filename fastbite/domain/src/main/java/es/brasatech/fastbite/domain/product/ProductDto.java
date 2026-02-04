package es.brasatech.fastbite.domain.product;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Product management in BackOffice.
 * This differs from the menu Product record as it includes additional fields.
 */
public record ProductDto(
                String id,
                String name,
                BigDecimal price,
                String description,
                String image,
                List<String> customizations, // List of customization IDs
                boolean active) {
}
