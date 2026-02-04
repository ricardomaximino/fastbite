package es.brasatech.fastbite.mongodb.product;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document for Product translations.
 * Stores translations for non-default languages only.
 * Default language values are stored directly in the ProductDocument.
 */
@Document(collection = "product_translations")
@CompoundIndex(name = "product_lang_idx", def = "{'productId': 1, 'language': 1}", unique = true)
public class ProductTranslationDocument {

    @Id
    private String id;
    private String productId;
    private String language;
    private String name;
    private String description;

    public ProductTranslationDocument() {
    }

    public ProductTranslationDocument(String productId, String language, String name, String description) {
        this.productId = productId;
        this.language = language;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
