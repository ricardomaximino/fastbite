package es.brasatech.fastbite.listener;

import es.brasatech.fastbite.domain.event.OrderStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public record OrderStatusChangeListener() {

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        System.out.println(event.order());
//        messagingTemplate.convertAndSend("/topic/orders", event.order());
    }
}
