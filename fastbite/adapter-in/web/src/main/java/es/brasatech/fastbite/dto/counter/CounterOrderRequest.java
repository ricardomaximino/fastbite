package es.brasatech.fastbite.dto.counter;

import es.brasatech.fastbite.domain.order.CartItem;
import java.util.List;

public record CounterOrderRequest(
        List<CartItem> items,
        String tableId,
        String paymentMethod,
        boolean paid) {
}
