package es.brasatech.fastbite.application.order;

import es.brasatech.fastbite.domain.event.OrderStatusChangedEvent;
import es.brasatech.fastbite.domain.order.*;
import es.brasatech.fastbite.domain.product.ProductCustomizerI18n;
import es.brasatech.fastbite.domain.table.TableStatus;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    List<Order> findAll();

    Optional<Order> findById(String id);

    Order create(Order group);

    Optional<Order> update(String id, Order group);

    boolean delete(String id);

    void clear();

    List<Order> findActiveByTableId(String tableId);

    void setTableStatus(String tableId, TableStatus status);

    // ===== I18n Methods =====

    Optional<ProductCustomizerI18n> findI18nById(String id);

    void publishEvent(Object event);

    default Order createOrder(List<CartItem> cartItems, int orderNumber, OrderPaymentStatus orderPaymentStatus,
            OrderChannel orderChannel, String orderLanguage, String userId) {
        var order = new Order(cartItems, orderNumber, orderPaymentStatus, orderChannel, orderLanguage, userId);
        var savedOrder = create(order);
        publishEvent(new OrderStatusChangedEvent(savedOrder));
        return savedOrder;
    }

    default Order createOrder(List<CartItem> cartItems, int orderNumber, OrderPaymentStatus orderPaymentStatus,
            OrderChannel orderChannel, String orderLanguage) {
        return createOrder(cartItems, orderNumber, orderPaymentStatus, orderChannel, orderLanguage, null);
    }

    default void moveToNextStatus(String id) {
        var order = findById(id).orElseThrow(() -> new RuntimeException("Not Found"));
        var newOrder = order.next();
        update(id, newOrder);
        publishEvent(new OrderStatusChangedEvent(newOrder));
    }

    default void cancelOrder(String id, String cancelReason) {
        var order = findById(id).orElseThrow(() -> new RuntimeException("Not Found"));
        var cancelledOrder = order.cancel(cancelReason);
        update(id, cancelledOrder);
        publishEvent(new OrderStatusChangedEvent(cancelledOrder));
    }

    default void setOrderStatus(String id, OrderStatus orderStatus) {
        var order = findById(id).orElseThrow(() -> new RuntimeException("Not Found"));
        var newOrder = order.setStatus(orderStatus);
        update(id, newOrder);
        publishEvent(new OrderStatusChangedEvent(newOrder));
    }

    default List<Order> getAllOrder() {
        return findAll();
    }

}
