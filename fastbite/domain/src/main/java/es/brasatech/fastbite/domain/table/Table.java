package es.brasatech.fastbite.domain.table;

import java.util.List;

public record Table(String id, String name, int seats, TableStatus status, boolean active, List<String> orderIds) {
    public Table(String name, int seats) {
        this(java.util.UUID.randomUUID().toString(), name, seats, TableStatus.AVAILABLE, true, List.of());
    }
}
