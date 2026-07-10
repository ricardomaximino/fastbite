package es.brasatech.fastbite.application.discount;

import es.brasatech.fastbite.domain.discount.DiscountRule;
import es.brasatech.fastbite.domain.discount.DiscountRuleI18n;
import es.brasatech.fastbite.domain.order.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DiscountService {
    List<DiscountRule> findAll();

    Optional<DiscountRule> findById(String id);

    DiscountRule create(DiscountRule rule);

    Optional<DiscountRule> update(String id, DiscountRule rule);

    boolean delete(String id);

    Optional<DiscountRuleI18n> findI18nById(String id);

    void updateI18n(String id, DiscountRuleI18n i18n);

    /**
     * Calculates the total discount reduction based on cart items, coupon validation,
     * and automatic discount rules currently active.
     */
    BigDecimal calculateDiscount(List<CartItem> items, String couponCode, String tableId);

    BigDecimal calculateDiscount(List<CartItem> items, String couponCode, String tableId, es.brasatech.fastbite.domain.order.OrderChannel channel);

    record CartBreakdown(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tax,
            BigDecimal total
    ) {}

    default CartBreakdown calculateCartBreakdown(List<CartItem> cartItems, String couponCode, String tableId, double taxPercentage) {
        var subtotal = cartItems.stream().map(CartItem::totalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = calculateDiscount(cartItems, couponCode, tableId, es.brasatech.fastbite.domain.order.OrderChannel.TABLE);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        if (discountedSubtotal.compareTo(BigDecimal.ZERO) < 0) {
            discountedSubtotal = BigDecimal.ZERO;
        }

        BigDecimal tax = discountedSubtotal.multiply(BigDecimal.valueOf(taxPercentage / 100)).setScale(2, java.math.RoundingMode.CEILING);
        BigDecimal total = discountedSubtotal.setScale(2, java.math.RoundingMode.HALF_UP);

        return new CartBreakdown(
                subtotal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : subtotal,
                discount,
                tax,
                total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : total
        );
    }
}
