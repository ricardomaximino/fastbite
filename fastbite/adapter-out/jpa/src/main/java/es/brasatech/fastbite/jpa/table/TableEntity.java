package es.brasatech.fastbite.jpa.table;

import jakarta.persistence.*;
import es.brasatech.fastbite.domain.table.TableStatus;

@Entity(name = "Table")
@Table(name = "dining_tables")
public class TableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private int seats;
    @Enumerated(EnumType.STRING)
    private TableStatus status;
    private boolean active;

    @ElementCollection
    @CollectionTable(name = "table_orders", joinColumns = @JoinColumn(name = "table_id"))
    @Column(name = "order_id")
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
