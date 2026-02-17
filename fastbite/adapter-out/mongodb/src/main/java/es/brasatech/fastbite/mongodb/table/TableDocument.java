package es.brasatech.fastbite.mongodb.table;

import es.brasatech.fastbite.domain.table.TableStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tables")
public class TableDocument {
    @Id
    private String id;
    private String name;
    private int seats;
    private TableStatus status;
    private boolean active;
    private java.util.List<String> orderIds = new java.util.ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public TableStatus getStatus() {
        return status;
    }

    public void setStatus(TableStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public java.util.List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(java.util.List<String> orderIds) {
        this.orderIds = orderIds;
    }
}
