package es.brasatech.fastbite.domain.discount;

import java.math.BigDecimal;

public record DiscountRule(
    String id,
    String name,
    DiscountScope scope,
    DiscountType type,
    BigDecimal value,
    BigDecimal minSubtotal,
    String couponCode, // Null for automatic discount
    boolean active,
    boolean accumulative,
    boolean applyOnCounter
) {
    public DiscountRule(String name, DiscountScope scope, DiscountType type, BigDecimal value, BigDecimal minSubtotal, String couponCode) {
        this(null, name, scope, type, value, minSubtotal, couponCode, true, false, false);
    }
}
