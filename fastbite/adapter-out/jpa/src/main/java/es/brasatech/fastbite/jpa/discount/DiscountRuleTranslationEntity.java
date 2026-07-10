package es.brasatech.fastbite.jpa.discount;

import jakarta.persistence.*;

@Entity
@Table(name = "discount_rule_translations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"discount_rule_id", "language"})
})
public class DiscountRuleTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_rule_id", nullable = false)
    private DiscountRuleEntity discountRule;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String name;

    public DiscountRuleTranslationEntity() {}

    public DiscountRuleTranslationEntity(DiscountRuleEntity discountRule, String language, String name) {
        this.discountRule = discountRule;
        this.language = language;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DiscountRuleEntity getDiscountRule() {
        return discountRule;
    }

    public void setDiscountRule(DiscountRuleEntity discountRule) {
        this.discountRule = discountRule;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
