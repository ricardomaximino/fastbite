package es.brasatech.fastbite.jpa.product;

import jakarta.persistence.*;

/**
 * Translation entity for Product.
 * Stores translations for non-default languages only.
 * Default language values are stored directly in the Product entity.
 */
@Entity(name = "ProductTranslation")
@Table(name = "product_translations", uniqueConstraints = @UniqueConstraint(columnNames = { "product_id",
        "language" }), indexes = @Index(name = "idx_product_translation_lang", columnList = "product_id, language"))
public class ProductTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(nullable = true)
    private String name;

    @Column(length = 1000, nullable = true)
    private String description;

    public ProductTranslationEntity() {
    }

    public ProductTranslationEntity(ProductEntity product, String language, String name, String description) {
        this.product = product;
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

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
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
