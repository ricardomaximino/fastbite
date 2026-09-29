package es.brasatech.fastbite.jpa.group;

import jakarta.persistence.*;

import java.util.List;
import es.brasatech.fastbite.jpa.i18n.Translatable;
import es.brasatech.fastbite.jpa.i18n.TranslationsConverter;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA entity for Group.
 * Stores default language values directly.
 * Translations for other languages are in the translations column.
 */
@Entity(name = "Group")
@Table(name = "groups")
public class GroupEntity implements Translatable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String icon;

    @ElementCollection
    @CollectionTable(name = "group_products", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "product_id")
    private List<String> products;

    /** Other languages of the text fields: language -> field -> text. */
    @Convert(converter = TranslationsConverter.class)
    @Column(name = "translations", columnDefinition = "text")
    private Map<String, Map<String, String>> translations = new HashMap<>();

    public GroupEntity() {
    }

    public GroupEntity(String id, String name, String description, String icon, List<String> products) {
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

    @Override
    public Map<String, Map<String, String>> getTranslations() {
        return translations;
    }

    @Override
    public void setTranslations(Map<String, Map<String, String>> translations) {
        this.translations = translations != null ? translations : new HashMap<>();
    }
}
