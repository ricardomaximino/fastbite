package es.brasatech.fastbite.config;

import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.customization.CustomizationDto;
import es.brasatech.fastbite.domain.customization.CustomizationI18n;
import es.brasatech.fastbite.domain.customization.CustomizationOptionDto;
import es.brasatech.fastbite.domain.customization.CustomizationOptionI18n;
import es.brasatech.fastbite.domain.group.Group;
import es.brasatech.fastbite.domain.group.GroupI18n;
import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.domain.order.OrderChannel;
import es.brasatech.fastbite.domain.order.OrderPaymentStatus;
import es.brasatech.fastbite.domain.payment.PaymentConfig;
import es.brasatech.fastbite.domain.product.ProductCustomizer;
import es.brasatech.fastbite.domain.product.ProductDto;
import es.brasatech.fastbite.domain.product.ProductI18n;
import es.brasatech.fastbite.domain.table.Table;
import es.brasatech.fastbite.domain.table.TableI18n;
import es.brasatech.fastbite.domain.table.TableStatus;
import es.brasatech.fastbite.domain.user.Customer;
import es.brasatech.fastbite.dto.counter.CounterOrderRequest;
import es.brasatech.fastbite.dto.menu.*;
import es.brasatech.fastbite.dto.office.BackOfficeDto;
import es.brasatech.fastbite.dto.order.OrderCancelReason;
import es.brasatech.fastbite.dto.order.OrderStatusChange;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

public class WebAdapterHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources().registerPattern("static/**");
        hints.resources().registerPattern("templates/**");
        hints.resources().registerResourceBundle("i18n/messages");

        // Serialization hints for session-stored objects
        hints.serialization().registerType(java.math.BigDecimal.class);
        hints.serialization().registerType(java.math.BigInteger.class);
        hints.serialization().registerType(java.util.ArrayList.class);
        hints.serialization().registerType(java.util.HashMap.class);
        hints.serialization().registerType(TypeReference.of(java.util.Collections.emptyList().getClass()));
        hints.serialization().registerType(CartItem.class);
        hints.serialization().registerType(ProductCustomizer.class);

        // Reflection hints for DTOs and Domain objects used in Thymeleaf/SpEL
        hints.reflection().registerType(TypeReference.of(java.math.BigDecimal.class), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(TypeReference.of(java.math.BigInteger.class), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
        
        // Explicitly register Thymeleaf utility classes for SpEL
        hints.reflection().registerType(org.thymeleaf.expression.Lists.class, MemberCategory.INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(org.thymeleaf.expression.Numbers.class, MemberCategory.INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(org.thymeleaf.expression.Maps.class, MemberCategory.INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(org.thymeleaf.expression.Strings.class, MemberCategory.INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(org.thymeleaf.expression.Messages.class, MemberCategory.INVOKE_PUBLIC_METHODS);

        hints.reflection().registerType(TypeReference.of(CounterOrderRequest.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(MenuData.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(Product.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(Customization.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(CustomizationType.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(CustomizationOption.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(CustomizationInputType.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(Tab.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(OrderDto.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(Cart.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(CartItem.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(BackOfficeDto.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(OrderCancelReason.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(OrderStatusChange.class), MemberCategory.values());

        // Domain objects used in templates or JSON serialization
        hints.reflection().registerType(TypeReference.of(ProductCustomizer.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(Order.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(OrderChannel.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(OrderPaymentStatus.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(TableStatus.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(Customer.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(Table.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(PaymentConfig.class), MemberCategory.values());

        // I18n DTOs for BackOffice
        hints.reflection().registerType(TypeReference.of(GroupI18n.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(ProductI18n.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(CustomizationI18n.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(CustomizationOptionI18n.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(TableI18n.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(I18nField.class), MemberCategory.values());
        
        // Missing Domain DTOs
        hints.reflection().registerType(TypeReference.of(CustomizationDto.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(CustomizationOptionDto.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(Group.class), MemberCategory.values());
        hints.reflection().registerType(TypeReference.of(ProductDto.class), MemberCategory.values());
    }
}
