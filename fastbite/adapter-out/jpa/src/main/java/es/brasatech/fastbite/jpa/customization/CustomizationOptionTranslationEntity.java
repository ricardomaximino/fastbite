package es.brasatech.fastbite.jpa.customization;

import jakarta.persistence.*;

/**
 * Translation entity for CustomizationOption.
 * Stores translations for non-default languages only.
 * Now references CustomizationOptionEntity directly for efficient lookups.
 */
@Entity(name = "CustomizationOptionTranslation")
@Table(name = "customization_option_translations", uniqueConstraints = @UniqueConstraint(columnNames = {
        "customization_option_id",
        "language" }), indexes = @Index(name = "idx_customization_option_translation_lang", columnList = "customization_option_id, language"))
public class CustomizationOptionTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customization_option_id", nullable = false)
    private CustomizationOptionEntity customizationOption;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(nullable = true)
    private String name;

    public CustomizationOptionTranslationEntity() {
    }

    public CustomizationOptionTranslationEntity(CustomizationOptionEntity customizationOption,
            String language, String name) {
        this.customizationOption = customizationOption;
        this.language = language;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CustomizationOptionEntity getCustomizationOption() {
        return customizationOption;
    }

    public void setCustomizationOption(CustomizationOptionEntity customizationOption) {
        this.customizationOption = customizationOption;
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
