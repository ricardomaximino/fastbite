package es.brasatech.fastbite.domain.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record Order(List<CartItem> items, int orderNumber, String id, LocalDateTime createdAt, LocalDateTime updatedAt,
        OrderStatus status, BigDecimal total, String cancelReason, OrderPaymentStatus paymentStatus,
        OrderChannel orderChannel, String orderLanguage, String userId) {

    public Order(List<CartItem> cartItems, int orderNumber, OrderPaymentStatus paymentStatus, OrderChannel orderChannel,
            String orderLanguage, String userId) {
        var now = LocalDateTime.now();
        var total = cartItems.stream().map(CartItem::totalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        this(cartItems, orderNumber, UUID.randomUUID().toString(), now, now, OrderStatus.CREATED, total, null,
                paymentStatus, orderChannel, orderLanguage, userId);
    }

    public Order(List<CartItem> cartItems, int orderNumber, OrderPaymentStatus paymentStatus, OrderChannel orderChannel,
            String orderLanguage) {
        this(cartItems, orderNumber, paymentStatus, orderChannel, orderLanguage, null);
    }

    public Order next() {
        if (status == OrderStatus.CREATED) {
            return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), OrderStatus.ACCEPTED, total, null,
                    paymentStatus, orderChannel, orderLanguage, userId);
        }
        if (status == OrderStatus.ACCEPTED) {
            return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), OrderStatus.PROCESSING, total,
                    null, paymentStatus, orderChannel, orderLanguage, userId);
        }
        if (status == OrderStatus.PROCESSING) {
            return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), OrderStatus.DONE, total, null,
                    paymentStatus, orderChannel, orderLanguage, userId);
        }
        if (status == OrderStatus.DONE) {
            return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), OrderStatus.DELIVERED, total, null,
                    paymentStatus, orderChannel, orderLanguage, userId);
        }
        return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), OrderStatus.COMPLETE, total, null,
                paymentStatus, orderChannel, orderLanguage, userId);
    }

    public Order previous() {
        if (status == OrderStatus.ACCEPTED) {
            return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), OrderStatus.CREATED, total, null,
                    paymentStatus, orderChannel, orderLanguage, userId);
        }
        if (status == OrderStatus.PROCESSING) {
            return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), OrderStatus.ACCEPTED, total, null,
                    paymentStatus, orderChannel, orderLanguage, userId);
        }
        if (status == OrderStatus.DONE) {
            return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), OrderStatus.PROCESSING, total,
                    null, paymentStatus, orderChannel, orderLanguage, userId);
        }
        if (status == OrderStatus.DELIVERED) {
            return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), OrderStatus.DONE, total, null,
                    paymentStatus, orderChannel, orderLanguage, userId);
        }
        if (status == OrderStatus.COMPLETE) {
            return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), OrderStatus.DELIVERED, total, null,
                    paymentStatus, orderChannel, orderLanguage, userId);
        }
        return this;
    }

    public Order cancel(String cancelReason) {
        return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), OrderStatus.CANCELLED, total,
                cancelReason, paymentStatus, orderChannel, orderLanguage, userId);
    }

    public Order setStatus(OrderStatus status) {
        return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), status, total, null, paymentStatus,
                orderChannel, orderLanguage, userId);
    }

    public Order setPaymentStatus(OrderPaymentStatus paymentStatus) {
        return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), status, total, null, paymentStatus,
                orderChannel, orderLanguage, userId);
    }

    public Order setChannel(OrderChannel orderChannel) {
        return new Order(items, orderNumber, id, createdAt, LocalDateTime.now(), status, total, null, paymentStatus,
                orderChannel, orderLanguage, userId);
    }

}
