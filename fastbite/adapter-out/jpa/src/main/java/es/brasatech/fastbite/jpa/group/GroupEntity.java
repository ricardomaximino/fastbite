package es.brasatech.fastbite.jpa.group;

import jakarta.persistence.*;

import java.util.List;

/**
 * JPA entity for Group.
 * Stores default language values directly.
 * Translations for other languages are in GroupTranslation table.
 */
@Entity(name = "Group")
@Table(name = "groups")
public class GroupEntity {

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
}
