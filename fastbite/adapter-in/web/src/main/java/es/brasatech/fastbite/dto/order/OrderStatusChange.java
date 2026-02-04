package es.brasatech.fastbite.dto.order;

import es.brasatech.fastbite.domain.order.OrderStatus;

public record OrderStatusChange(OrderStatus value) {
}
