package es.brasatech.fastbite.dto.menu;

import es.brasatech.fastbite.domain.order.CartItem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        String id,
        LocalDateTime dateTime,
        Double tax,
        BigDecimal taxAmount,
        List<CartItem> itemList,
        BigDecimal subtotal,
        BigDecimal total) implements Serializable {
    public OrderDto(String id, List<CartItem> itemList) {
        var total = itemList.stream().map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var subTotal = total;
        var tax = 10D;
        var taxAmount = total.multiply(BigDecimal.valueOf(tax)).divide(BigDecimal.valueOf(100));
        this(id, LocalDateTime.now(), tax, taxAmount, itemList, subTotal, total);
    }
}
