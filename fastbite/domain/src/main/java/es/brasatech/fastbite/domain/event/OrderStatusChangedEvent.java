package es.brasatech.fastbite.domain.event;


import es.brasatech.fastbite.domain.order.Order;

public record OrderStatusChangedEvent(Order order) {}
