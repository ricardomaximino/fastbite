package es.brasatech.fastbite.mongodb.customization;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document for Customization translations.
 * Stores translations for non-default languages only.
 */
@Document(collection = "customization_translations")
@CompoundIndex(name = "customization_lang_idx", def = "{'customizationId': 1, 'language': 1}", unique = true)
public class CustomizationTranslationDocument {

    @Id
    private String id;
    private String customizationId;
    private String language;
    private String name;

    public CustomizationTranslationDocument() {
    }

    public CustomizationTranslationDocument(String customizationId, String language, String name) {
        this.customizationId = customizationId;
        this.language = language;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomizationId() {
        return customizationId;
    }

    public void setCustomizationId(String customizationId) {
        this.customizationId = customizationId;
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
