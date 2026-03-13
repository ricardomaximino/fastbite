package es.brasatech.fastbite.mongodb.product;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * MongoDB document for Product entity.
 * Stores default language values directly.
 * Translations for other languages are in ProductTranslationDocument
 * collection.
 */
@Document(collection = "products")
public class ProductDocument {

    @Id
    private String id;
    private String name;
    private BigDecimal price;
    private String description;
    private String image;
    private Set<String> customizations = new HashSet<>();
    private boolean active;

    public ProductDocument() {
    }

    public ProductDocument(String id, String name, BigDecimal price, String description,
            String image, Set<String> customizations, boolean active) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.image = image;
        this.customizations = customizations;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Set<String> getCustomizations() {
        return customizations;
    }

    public void setCustomizations(Set<String> customizations) {
        this.customizations = customizations;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
