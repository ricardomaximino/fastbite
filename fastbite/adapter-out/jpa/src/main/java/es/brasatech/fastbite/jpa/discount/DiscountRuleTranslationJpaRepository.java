package es.brasatech.fastbite.jpa.discount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountRuleTranslationJpaRepository extends JpaRepository<DiscountRuleTranslationEntity, Long> {
    List<DiscountRuleTranslationEntity> findAllByDiscountRuleId(String ruleId);
    void deleteByDiscountRuleId(String ruleId);
}
