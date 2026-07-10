package es.brasatech.fastbite.jpa.discount;

import es.brasatech.fastbite.application.order.OrderService;
import es.brasatech.fastbite.domain.discount.DiscountScope;
import es.brasatech.fastbite.domain.discount.DiscountType;
import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.domain.order.OrderChannel;
import es.brasatech.fastbite.domain.order.OrderPaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DiscountServiceTest {

    private DiscountRuleJpaRepository repository;
    private DiscountRuleTranslationJpaRepository translationRepository;
    private OrderService orderService;
    private DiscountServiceJpaImpl discountService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(DiscountRuleJpaRepository.class);
        translationRepository = Mockito.mock(DiscountRuleTranslationJpaRepository.class);
        orderService = Mockito.mock(OrderService.class);
        discountService = new DiscountServiceJpaImpl(repository, translationRepository, null, orderService);
    }

    @Test
    void testCalculateDiscount_NoActiveRules() {
        when(repository.findAll()).thenReturn(Collections.emptyList());
        List<CartItem> items = List.of(new CartItem("1", "item-1", "Kebab", "Kebab desc", "image.jpg", 1, Collections.emptyList(), BigDecimal.valueOf(10.00)));
        
        BigDecimal discount = discountService.calculateDiscount(items, null, null);
        assertEquals(BigDecimal.ZERO.setScale(2), discount);
    }

    @Test
    void testCalculateDiscount_OrderScopeAutomaticPercentage() {
        DiscountRuleEntity rule = new DiscountRuleEntity();
        rule.setId("rule-1");
        rule.setName("10% Off");
        rule.setScope(DiscountScope.ORDER);
        rule.setType(DiscountType.PERCENTAGE);
        rule.setValue(BigDecimal.valueOf(10.00));
        rule.setMinSubtotal(BigDecimal.valueOf(20.00));
        rule.setActive(true);

        when(repository.findAll()).thenReturn(List.of(rule));
        
        List<CartItem> items = List.of(
            new CartItem("1", "item-1", "Kebab", "Kebab desc", "image.jpg", 2, Collections.emptyList(), BigDecimal.valueOf(15.00))
        );

        // Subtotal = 30.00, matches minSubtotal (20.00), discount = 3.00
        BigDecimal discount = discountService.calculateDiscount(items, null, null);
        assertEquals(BigDecimal.valueOf(3.00).setScale(2), discount);
    }

    @Test
    void testCalculateDiscount_TableScopeFixedAmountCoupon() {
        DiscountRuleEntity rule = new DiscountRuleEntity();
        rule.setId("rule-2");
        rule.setName("€10 Off Group");
        rule.setScope(DiscountScope.TABLE);
        rule.setType(DiscountType.FIXED_AMOUNT);
        rule.setValue(BigDecimal.valueOf(10.00));
        rule.setMinSubtotal(BigDecimal.valueOf(50.00));
        rule.setCouponCode("GROUP50");
        rule.setActive(true);

        when(repository.findAll()).thenReturn(List.of(rule));

        // Table already has orders totaling 30.00
        Order activeOrder = new Order(new ArrayList<>(), 12, OrderPaymentStatus.UNPAID, OrderChannel.TABLE, "en", null, "Jose");
        // We override or adjust because record total field is evaluated based on empty items subtotal. Let's create an order with items to match 30.00 total.
        List<CartItem> activeItems = List.of(new CartItem("2", "item-2", "Soda", "Cold soda", "soda.jpg", 3, Collections.emptyList(), BigDecimal.valueOf(10.00)));
        OrderJose joseOrder = new OrderJose(activeItems);
        when(orderService.findActiveByTableId("table-3")).thenReturn(List.of(joseOrder.toOrder()));

        List<CartItem> items = List.of(
            new CartItem("1", "item-1", "Kebab", "Kebab desc", "image.jpg", 2, Collections.emptyList(), BigDecimal.valueOf(10.00))
        );

        // Order subtotal = 20.00. Table collective total = 20.00 + 30.00 = 50.00 (condition met).
        // Discount is €10.00 scaled down to this order size (20 / 50 = 40% of the €10 discount = €4.00)
        BigDecimal discount = discountService.calculateDiscount(items, "GROUP50", "table-3");
        assertEquals(BigDecimal.valueOf(4.00).setScale(2), discount);
    }

    @Test
    void testCalculateDiscount_AccumulativeRules() {
        DiscountRuleEntity rule1 = new DiscountRuleEntity();
        rule1.setId("rule-a");
        rule1.setName("Automatic €2 Off");
        rule1.setScope(DiscountScope.ORDER);
        rule1.setType(DiscountType.FIXED_AMOUNT);
        rule1.setValue(BigDecimal.valueOf(2.00));
        rule1.setMinSubtotal(BigDecimal.valueOf(10.00));
        rule1.setActive(true);
        rule1.setAccumulative(true);

        DiscountRuleEntity rule2 = new DiscountRuleEntity();
        rule2.setId("rule-b");
        rule2.setName("Automatic €3 Off");
        rule2.setScope(DiscountScope.ORDER);
        rule2.setType(DiscountType.FIXED_AMOUNT);
        rule2.setValue(BigDecimal.valueOf(3.00));
        rule2.setMinSubtotal(BigDecimal.valueOf(15.00));
        rule2.setActive(true);
        rule2.setAccumulative(true);

        when(repository.findAll()).thenReturn(List.of(rule1, rule2));

        List<CartItem> items = List.of(
            new CartItem("1", "item-1", "Kebab", "Kebab desc", "image.jpg", 2, Collections.emptyList(), BigDecimal.valueOf(10.00))
        );

        // Subtotal = 20.00. Since both are accumulative, both apply (2 + 3 = 5.00 discount)
        BigDecimal discount = discountService.calculateDiscount(items, null, null);
        assertEquals(BigDecimal.valueOf(5.00).setScale(2), discount);
    }

    @Test
    void testCalculateDiscount_NonAccumulativeRules() {
        DiscountRuleEntity rule1 = new DiscountRuleEntity();
        rule1.setId("rule-a");
        rule1.setName("Automatic €2 Off");
        rule1.setScope(DiscountScope.ORDER);
        rule1.setType(DiscountType.FIXED_AMOUNT);
        rule1.setValue(BigDecimal.valueOf(2.00));
        rule1.setMinSubtotal(BigDecimal.valueOf(10.00));
        rule1.setActive(true);
        rule1.setAccumulative(false); // Stop here if evaluated!

        DiscountRuleEntity rule2 = new DiscountRuleEntity();
        rule2.setId("rule-b");
        rule2.setName("Automatic €3 Off");
        rule2.setScope(DiscountScope.ORDER);
        rule2.setType(DiscountType.FIXED_AMOUNT);
        rule2.setValue(BigDecimal.valueOf(3.00));
        rule2.setMinSubtotal(BigDecimal.valueOf(15.00));
        rule2.setActive(true);
        rule2.setAccumulative(true);

        when(repository.findAll()).thenReturn(List.of(rule1, rule2));

        List<CartItem> items = List.of(
            new CartItem("1", "item-1", "Kebab", "Kebab desc", "image.jpg", 2, Collections.emptyList(), BigDecimal.valueOf(10.00))
        );

        // Subtotal = 20.00. Since rule1 is NOT accumulative, evaluation breaks immediately, yielding only 2.00 discount.
        BigDecimal discount = discountService.calculateDiscount(items, null, null);
        assertEquals(BigDecimal.valueOf(2.00).setScale(2), discount);
    }

    @Test
    void testCalculateDiscount_CounterChannelWithApplyOnCounter() {
        DiscountRuleEntity rule = new DiscountRuleEntity();
        rule.setId("rule-c1");
        rule.setName("10% Off");
        rule.setScope(DiscountScope.ORDER);
        rule.setType(DiscountType.PERCENTAGE);
        rule.setValue(BigDecimal.valueOf(10.00));
        rule.setMinSubtotal(BigDecimal.valueOf(10.00));
        rule.setActive(true);
        rule.setApplyOnCounter(true);

        when(repository.findAll()).thenReturn(List.of(rule));

        List<CartItem> items = List.of(
            new CartItem("1", "item-1", "Kebab", "Kebab desc", "image.jpg", 2, Collections.emptyList(), BigDecimal.valueOf(10.00))
        );

        BigDecimal discount = discountService.calculateDiscount(items, null, null, es.brasatech.fastbite.domain.order.OrderChannel.COUNTER);
        assertEquals(BigDecimal.valueOf(2.00).setScale(2), discount);
    }

    @Test
    void testCalculateDiscount_CounterChannelWithoutApplyOnCounter() {
        DiscountRuleEntity rule = new DiscountRuleEntity();
        rule.setId("rule-c2");
        rule.setName("10% Off");
        rule.setScope(DiscountScope.ORDER);
        rule.setType(DiscountType.PERCENTAGE);
        rule.setValue(BigDecimal.valueOf(10.00));
        rule.setMinSubtotal(BigDecimal.valueOf(10.00));
        rule.setActive(true);
        rule.setApplyOnCounter(false); // Unchecked by default!

        when(repository.findAll()).thenReturn(List.of(rule));

        List<CartItem> items = List.of(
            new CartItem("1", "item-1", "Kebab", "Kebab desc", "image.jpg", 2, Collections.emptyList(), BigDecimal.valueOf(10.00))
        );

        BigDecimal discount = discountService.calculateDiscount(items, null, null, es.brasatech.fastbite.domain.order.OrderChannel.COUNTER);
        assertEquals(BigDecimal.ZERO.setScale(2), discount);
    }

    private static class OrderJose {
        private final List<CartItem> items;
        public OrderJose(List<CartItem> items) {
            this.items = items;
        }
        public Order toOrder() {
            return new Order(items, 12, OrderPaymentStatus.UNPAID, OrderChannel.TABLE, "en", null, "Jose");
        }
    }
}
