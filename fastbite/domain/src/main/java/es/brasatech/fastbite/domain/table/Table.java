package es.brasatech.fastbite.domain.table;

public record Table(String id, String name, int seats, TableStatus status, boolean active) {
    public Table(String name, int seats) {
        this(java.util.UUID.randomUUID().toString(), name, seats, TableStatus.AVAILABLE, true);
    }
}
