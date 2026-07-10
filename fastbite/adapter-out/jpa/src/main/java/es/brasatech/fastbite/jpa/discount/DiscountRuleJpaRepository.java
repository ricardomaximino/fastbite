package es.brasatech.fastbite.jpa.discount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountRuleJpaRepository extends JpaRepository<DiscountRuleEntity, String> {
    Optional<DiscountRuleEntity> findByCouponCodeIgnoreCaseAndActiveTrue(String couponCode);
}
