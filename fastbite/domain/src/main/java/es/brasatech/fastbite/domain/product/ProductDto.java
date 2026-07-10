package es.brasatech.fastbite.domain.product;

import java.math.BigDecimal;
import java.util.Set;

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
                Set<String> customizations, // Set of customization IDs
                boolean active) {

    public Product toDomain() {
        return new Product(id, name, price, description, image, customizations, active);
    }

    public static ProductDto fromDomain(Product domain) {
        if (domain == null) {
            return null;
        }
        return new ProductDto(
                domain.getId(),
                domain.getName(),
                domain.getPrice(),
                domain.getDescription(),
                domain.getImage(),
                domain.getCustomizations(),
                domain.isActive()
        );
    }
}
