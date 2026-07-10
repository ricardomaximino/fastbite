package es.brasatech.fastbite.domain.discount;

import es.brasatech.fastbite.domain.I18nField;

import java.math.BigDecimal;

public record DiscountRuleI18n(
    String id,
    I18nField name,
    DiscountScope scope,
    DiscountType type,
    BigDecimal value,
    BigDecimal minSubtotal,
    String couponCode,
    boolean active,
    boolean accumulative,
    boolean applyOnCounter
) {}
