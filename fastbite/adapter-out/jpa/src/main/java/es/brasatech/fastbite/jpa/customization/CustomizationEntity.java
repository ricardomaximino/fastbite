package es.brasatech.fastbite.jpa.customization;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for Customization.
 * Stores default language values directly.
 * Translations for other languages are in CustomizationTranslation table.
 * Options are now separate entities with @OneToMany relationship for efficient
 * queries.
 */
@Entity(name = "Customizations")
@Table(name = "customizations")
public class CustomizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @OneToMany(mappedBy = "customization", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("optionIndex ASC")
    private List<CustomizationOptionEntity> options = new ArrayList<>();

    @Column(nullable = false)
    private int usageCount = 0;

    public CustomizationEntity() {
    }

    public CustomizationEntity(String id, String name, String type, int usageCount) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.usageCount = usageCount;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<CustomizationOptionEntity> getOptions() {
        return options;
    }

    public void setOptions(List<CustomizationOptionEntity> options) {
        // Clear existing options
        this.options.clear();
        if (options != null) {
            options.forEach(this::addOption);
        }
    }

    /**
     * Helper method to add an option and maintain bidirectional relationship
     */
    public void addOption(CustomizationOptionEntity option) {
        options.add(option);
        option.setCustomization(this);
    }

    /**
     * Helper method to remove an option and maintain bidirectional relationship
     */
    public void removeOption(CustomizationOptionEntity option) {
        options.remove(option);
        option.setCustomization(null);
    }

    /**
     * Helper method to clear all options
     */
    public void clearOptions() {
        options.forEach(option -> option.setCustomization(null));
        options.clear();
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }
}
