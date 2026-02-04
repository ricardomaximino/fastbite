package es.brasatech.fastbite.domain.order;

import es.brasatech.fastbite.domain.product.ProductCustomizer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record CartItem(
        String id,
        String itemId,
        String name,
        String description,
        String image,
        int quantity,
        List<ProductCustomizer> customizations,
        BigDecimal price) implements Serializable {

    public BigDecimal totalPrice() {
        if (customizations == null || customizations.isEmpty()) {
            return calculatePrice();
        }
        return customizations.stream().map(ProductCustomizer::price).reduce(calculatePrice(), BigDecimal::add);
    }

    private BigDecimal calculatePrice() {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
