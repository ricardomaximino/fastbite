package es.brasatech.fastbite.jpa.table;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "table_translations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TableTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private TableEntity table;

    @Column(nullable = false)
    private String language;

    private String name;

    public TableTranslationEntity(TableEntity table, String language, String name) {
        this.table = table;
        this.language = language;
        this.name = name;
    }
}
