package es.brasatech.fastbite.mongodb.customization;

import es.brasatech.fastbite.domain.customization.CustomizationOptionDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * MongoDB document for Customization entity.
 * Stores default language values directly.
 * Translations for other languages are in CustomizationTranslationDocument and
 * CustomizationOptionTranslationDocument collections.
 */
@Document(collection = "customizations")
public class CustomizationDocument {

    @Id
    private String id;
    private String name;
    private String type;
    private List<CustomizationOptionDto> options;
    private int usageCount;

    public CustomizationDocument() {
    }

    public CustomizationDocument(String id, String name, String type,
            List<CustomizationOptionDto> options, int usageCount) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.options = options;
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

    public List<CustomizationOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<CustomizationOptionDto> options) {
        this.options = options;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }
}
