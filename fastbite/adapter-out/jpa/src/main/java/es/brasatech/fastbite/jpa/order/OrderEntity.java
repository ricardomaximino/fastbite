package es.brasatech.fastbite.jpa.order;

import es.brasatech.fastbite.domain.order.OrderChannel;
import es.brasatech.fastbite.domain.order.OrderPaymentStatus;
import es.brasatech.fastbite.domain.order.OrderStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "Order")
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private List<CartItemEntity> items;
    private int orderNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private OrderStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    private String cancelReason;
    private OrderPaymentStatus paymentStatus;
    private OrderChannel orderChannel;
    private String orderLanguage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CartItemEntity> getItems() {
        return items;
    }

    public void setItems(List<CartItemEntity> items) {
        this.items = items;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public OrderPaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(OrderPaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public OrderChannel getOrderChannel() {
        return orderChannel;
    }

    public void setOrderChannel(OrderChannel orderChannel) {
        this.orderChannel = orderChannel;
    }

    public String getOrderLanguage() {
        return orderLanguage;
    }

    public void setOrderLanguage(String orderLanguage) {
        this.orderLanguage = orderLanguage;
    }
}
