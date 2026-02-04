package es.brasatech.fastbite.jpa.customization;

import jakarta.persistence.*;

/**
 * Translation entity for Customization.
 * Stores translations for non-default languages only.
 * Default language values are stored directly in the Customization entity.
 */
@Entity(name = "CustomizationTranslation")
@Table(name = "customization_translations", uniqueConstraints = @UniqueConstraint(columnNames = { "customization_id",
        "language" }), indexes = @Index(name = "idx_customization_translation_lang", columnList = "customization_id, language"))
public class CustomizationTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customization_id", nullable = false)
    private CustomizationEntity customization;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(nullable = true)
    private String name;

    public CustomizationTranslationEntity() {
    }

    public CustomizationTranslationEntity(CustomizationEntity customization, String language, String name) {
        this.customization = customization;
        this.language = language;
        this.name = name;
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
