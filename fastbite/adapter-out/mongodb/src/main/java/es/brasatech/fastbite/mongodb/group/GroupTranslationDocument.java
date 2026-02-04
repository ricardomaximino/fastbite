package es.brasatech.fastbite.mongodb.group;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document for Group translations.
 * Stores translations for non-default languages only.
 */
@Document(collection = "group_translations")
@CompoundIndex(name = "group_lang_idx", def = "{'groupId': 1, 'language': 1}", unique = true)
public class GroupTranslationDocument {

    @Id
    private String id;
    private String groupId;
    private String language;
    private String name;
    private String description;

    public GroupTranslationDocument() {
    }

    public GroupTranslationDocument(String groupId, String language, String name, String description) {
        this.groupId = groupId;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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
