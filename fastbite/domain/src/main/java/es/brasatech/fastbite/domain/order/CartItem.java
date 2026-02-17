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
        BigDecimal unitPrice = price != null ? price : BigDecimal.ZERO;
        if (customizations != null && !customizations.isEmpty()) {
            unitPrice = customizations.stream()
                    .map(ProductCustomizer::price)
                    .reduce(unitPrice, BigDecimal::add);
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
