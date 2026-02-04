package es.brasatech.fastbite.jpa.product;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * JPA entity for Product.
 * Stores default language values directly.
 * Translations for other languages are in ProductTranslation table.
 */
@Entity(name = "Product")
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(length = 1000)
    private String description;

    private String image;

    @ElementCollection
    @CollectionTable(name = "product_customizations", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "customization_id")
    private List<String> customizations;

    @Column(nullable = false)
    private boolean active = true;

    public ProductEntity() {
    }

    public ProductEntity(String id, String name, BigDecimal price, String description,
            String image, List<String> customizations, boolean active) {
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

    public List<String> getCustomizations() {
        return customizations;
    }

    public void setCustomizations(List<String> customizations) {
        this.customizations = customizations;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
