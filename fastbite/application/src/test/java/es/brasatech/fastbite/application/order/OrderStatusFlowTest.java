package es.brasatech.fastbite.application.order;

import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.domain.order.OrderChannel;
import es.brasatech.fastbite.domain.order.OrderPaymentStatus;
import es.brasatech.fastbite.domain.order.OrderStatus;
import es.brasatech.fastbite.domain.order.ServiceType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusFlowTest {

    private static CartItem item(String price, int quantity) {
        return new CartItem("line", "product", "Kebab", null, null, quantity, List.of(), new BigDecimal(price));
    }

    private static Order takeaway() {
        return new Order(List.of(item("6.50", 2)), 7, OrderPaymentStatus.UNPAID, OrderChannel.COUNTER, "en",
                "cashier", "Marta", ServiceType.TAKEAWAY);
    }

    @Test
    void anOrderMovesForwardOneStepAtATimeAndStopsWhenComplete() {
        List<OrderStatus> visited = new ArrayList<>();
        Order order = takeaway();
        for (int step = 0; step < 7; step++) {
            visited.add(order.status());
            order = order.next();
        }

        assertThat(visited).containsExactly(OrderStatus.CREATED, OrderStatus.ACCEPTED, OrderStatus.PROCESSING,
                OrderStatus.DONE, OrderStatus.DELIVERED, OrderStatus.COMPLETE, OrderStatus.COMPLETE);
    }

    @Test
    void anOrderMovesBackOneStepAtATimeAndStopsAtCreated() {
        Order order = takeaway().setStatus(OrderStatus.COMPLETE);

        assertThat(order.previous().status()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.previous().previous().previous().previous().previous().status()).isEqualTo(OrderStatus.CREATED);
        Order created = takeaway();
        assertThat(created.previous()).isSameAs(created);
    }

    @Test
    void aCancelledOrderStaysCancelledGoingBackAndIsClosedGoingForward() {
        Order cancelled = takeaway().cancel("No show");

        assertThat(cancelled.cancelReason()).isEqualTo("No show");
        assertThat(cancelled.previous()).isSameAs(cancelled);
        assertThat(cancelled.next().status()).isEqualTo(OrderStatus.COMPLETE);
    }

    @Test
    void everyChangeKeepsWhoTheOrderIsForAndHowItIsServed() {
        Order order = takeaway();
        List<Order> changed = List.of(order.next(), order.cancel("x"), order.setPaymentStatus(OrderPaymentStatus.PAID),
                order.setChannel(OrderChannel.ONLINE), order.withItems(List.of(item("1.00", 1))));

        assertThat(changed).allSatisfy(after -> {
            assertThat(after.id()).isEqualTo(order.id());
            assertThat(after.orderNumber()).isEqualTo(7);
            assertThat(after.customerName()).isEqualTo("Marta");
            assertThat(after.userId()).isEqualTo("cashier");
            assertThat(after.serviceType()).isEqualTo(ServiceType.TAKEAWAY);
        });
    }

    @Test
    void newLinesChangeTheTotalButNotThePaymentOrTheStatus() {
        Order order = takeaway().next();
        assertThat(order.total()).isEqualByComparingTo("13.00");

        Order edited = order.withItems(List.of(item("6.50", 3), item("2.00", 1)));

        assertThat(edited.total()).isEqualByComparingTo("21.50");
        assertThat(edited.subtotal()).isEqualByComparingTo("21.50");
        assertThat(edited.status()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(edited.paymentStatus()).isEqualTo(OrderPaymentStatus.UNPAID);
    }

    @Test
    void ordersCreatedWithoutAServiceTypeAreTableOrders() {
        Order order = new Order(List.of(), 1, OrderPaymentStatus.UNPAID, OrderChannel.TABLE, "en");

        assertThat(order.serviceType()).isEqualTo(ServiceType.DINE_IN);
    }
}
