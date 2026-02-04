package es.brasatech.fastbite.mongodb.customization;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document for CustomizationOption translations.
 * Stores translations for non-default languages only.
 * Now includes optionId for efficient lookups.
 */
@Document(collection = "customization_option_translations")
@CompoundIndex(name = "option_lang_idx", def = "{'optionId': 1, 'language': 1}", unique = true)
@CompoundIndex(name = "customization_option_lang_idx", def = "{'customizationId': 1, 'optionIndex': 1, 'language': 1}")
public class CustomizationOptionTranslationDocument {

    @Id
    private String id;
    private String optionId; // Direct reference to option ID for efficient lookups
    private String customizationId; // Kept for batch operations
    private int optionIndex; // Kept for backward compatibility
    private String language;
    private String name;

    public CustomizationOptionTranslationDocument() {
    }

    public CustomizationOptionTranslationDocument(String optionId, String customizationId,
            int optionIndex, String language, String name) {
        this.optionId = optionId;
        this.customizationId = customizationId;
        this.optionIndex = optionIndex;
        this.language = language;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getCustomizationId() {
        return customizationId;
    }

    public void setCustomizationId(String customizationId) {
        this.customizationId = customizationId;
    }

    public int getOptionIndex() {
        return optionIndex;
    }

    public void setOptionIndex(int optionIndex) {
        this.optionIndex = optionIndex;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
