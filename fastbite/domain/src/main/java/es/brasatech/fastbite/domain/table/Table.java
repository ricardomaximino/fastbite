package es.brasatech.fastbite.domain.table;

public record Table(String id, String name, int capacity, boolean active) {
    public Table(String name, int capacity) {
        this(java.util.UUID.randomUUID().toString(), name, capacity, true);
    }
}
