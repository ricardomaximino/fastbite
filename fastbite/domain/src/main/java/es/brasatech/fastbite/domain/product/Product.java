package es.brasatech.fastbite.domain.product;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Domain model representing a Product.
 * Pure business logic representation decoupled from transport DTOs.
 */
public class Product {
    private final String id;
    private final String name;
    private final BigDecimal price;
    private final String description;
    private final String image;
    private final Set<String> customizations;
    private final boolean active;

    public Product(String id, String name, BigDecimal price, String description, String image, Set<String> customizations, boolean active) {
        this.id = id;
        this.name = name;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.description = description;
        this.image = image;
        this.customizations = customizations != null ? Set.copyOf(customizations) : Set.of();
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public Set<String> getCustomizations() {
        return customizations;
    }

    public boolean isActive() {
        return active;
    }
}
