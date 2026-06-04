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
        this(id,
             LocalDateTime.now(),
             10D,
             itemList.stream().map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity()))).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(BigDecimal.valueOf(10D)).divide(BigDecimal.valueOf(100)),
             itemList,
             itemList.stream().map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity()))).reduce(BigDecimal.ZERO, BigDecimal::add),
             itemList.stream().map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity()))).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
