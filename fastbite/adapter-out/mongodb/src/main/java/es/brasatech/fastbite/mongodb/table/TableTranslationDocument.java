package es.brasatech.fastbite.mongodb.table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "table_translations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TableTranslationDocument {

    @Id
    private String id;
    private String tableId;
    private String language;
    private String name;

    public TableTranslationDocument(String tableId, String language, String name) {
        this.tableId = tableId;
        this.language = language;
        this.name = name;
    }
}
