package es.brasatech.fastbite.jpa.customization;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * JPA entity for Customization Option.
 * Each option is now a separate entity with its own translations.
 * This allows efficient translation lookups for orders without losing parent
 * context.
 */
@Entity
@Table(name = "customization_options", indexes = {
        @Index(name = "idx_option_customization", columnList = "customization_id"),
        @Index(name = "idx_option_order", columnList = "customization_id, option_index")
})
public class CustomizationOptionEntity {

    @Id
    private String id; // Format: "customizationId-opt-index"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customization_id", nullable = false)
    private CustomizationEntity customization;

    @Column(nullable = false)
    private String name; // Default language value

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean isSelectedByDefault;

    @Column(nullable = false)
    private int defaultValue;

    @Column(name = "option_index", nullable = false)
    private int optionIndex; // To maintain order within customization

    public CustomizationOptionEntity() {
    }

    public CustomizationOptionEntity(String id, String name, BigDecimal price,
            boolean isSelectedByDefault, int defaultValue, int optionIndex) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.isSelectedByDefault = isSelectedByDefault;
        this.defaultValue = defaultValue;
        this.optionIndex = optionIndex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CustomizationEntity getCustomization() {
        return customization;
    }

    public void setCustomization(CustomizationEntity customization) {
        this.customization = customization;
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

    public boolean isSelectedByDefault() {
        return isSelectedByDefault;
    }

    public void setSelectedByDefault(boolean selectedByDefault) {
        isSelectedByDefault = selectedByDefault;
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getOptionIndex() {
        return optionIndex;
    }

    public void setOptionIndex(int optionIndex) {
        this.optionIndex = optionIndex;
    }
}
