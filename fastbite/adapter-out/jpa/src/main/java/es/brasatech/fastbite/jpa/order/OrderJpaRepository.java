package es.brasatech.fastbite.jpa.order;

import es.brasatech.fastbite.domain.order.OrderPaymentStatus;
import es.brasatech.fastbite.domain.order.OrderStatus;
import es.brasatech.fastbite.domain.order.ServiceType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@Profile("jpa")
public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {

    List<OrderEntity> findByServiceTypeAndPaymentStatusAndStatusNotInOrderByCreatedAt(ServiceType serviceType,
            OrderPaymentStatus paymentStatus, Collection<OrderStatus> closed);
}
