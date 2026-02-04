package es.brasatech.fastbite.listener;

import es.brasatech.fastbite.domain.event.OrderPaymentStatusChagedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public record OrderPaymentStatusChangedListener() {

    @EventListener
    public void onOrderPaymentStatusChanged(OrderPaymentStatusChagedEvent event) {
        System.out.println(event.order());
//        messagingTemplate.convertAndSend("/topic/payment", event.order());
    }
}
