package es.brasatech.fastbite.mongodb.group;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * MongoDB document for Group entity.
 * Stores default language values directly.
 * Translations for other languages are in GroupTranslationDocument collection.
 */
@Document(collection = "groups")
public class GroupDocument {

    @Id
    private String id;
    private String name;
    private String description;
    private String icon;
    private List<String> products;

    public GroupDocument() {
    }

    public GroupDocument(String id, String name, String description, String icon, List<String> products) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.products = products;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<String> getProducts() {
        return products;
    }

    public void setProducts(List<String> products) {
        this.products = products;
    }
}
