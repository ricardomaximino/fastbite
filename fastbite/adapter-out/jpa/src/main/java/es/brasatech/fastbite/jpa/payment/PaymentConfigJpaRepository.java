package es.brasatech.fastbite.jpa.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentConfigJpaRepository extends JpaRepository<PaymentConfigEntity, String> {
}
