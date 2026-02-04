package es.brasatech.fastbite.jpa.group;

import jakarta.persistence.*;

/**
 * Translation entity for Group.
 * Stores translations for non-default languages only.
 * Default language values are stored directly in the Group entity.
 */
@Entity(name = "GroupTranslation")
@Table(name = "group_translations", uniqueConstraints = @UniqueConstraint(columnNames = { "group_id",
        "language" }), indexes = @Index(name = "idx_group_translation_lang", columnList = "group_id, language"))
public class GroupTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(nullable = true)
    private String name;

    @Column(length = 1000, nullable = true)
    private String description;

    public GroupTranslationEntity() {
    }

    public GroupTranslationEntity(GroupEntity group, String language, String name, String description) {
        this.group = group;
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

    public GroupEntity getGroup() {
        return group;
    }

    public void setGroup(GroupEntity group) {
        this.group = group;
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
