package es.brasatech.fastbite.jpa.discount;

import es.brasatech.fastbite.domain.discount.DiscountScope;
import es.brasatech.fastbite.domain.discount.DiscountType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity(name = "DiscountRule")
@Table(name = "discount_rules")
public class DiscountRuleEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountScope scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal value;

    @Column(name = "min_subtotal", nullable = false)
    private BigDecimal minSubtotal;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "accumulative", nullable = false, columnDefinition = "boolean default false")
    private boolean accumulative = false;

    @Column(name = "apply_on_counter", nullable = false, columnDefinition = "boolean default false")
    private boolean applyOnCounter = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DiscountScope getScope() {
        return scope;
    }

    public void setScope(DiscountScope scope) {
        this.scope = scope;
    }

    public DiscountType getType() {
        return type;
    }

    public void setType(DiscountType type) {
        this.type = type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getMinSubtotal() {
        return minSubtotal;
    }

    public void setMinSubtotal(BigDecimal minSubtotal) {
        this.minSubtotal = minSubtotal;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAccumulative() {
        return accumulative;
    }

    public void setAccumulative(boolean accumulative) {
        this.accumulative = accumulative;
    }

    public boolean isApplyOnCounter() {
        return applyOnCounter;
    }

    public void setApplyOnCounter(boolean applyOnCounter) {
        this.applyOnCounter = applyOnCounter;
    }
}
