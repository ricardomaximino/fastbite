package es.brasatech.fastbite.domain.product;

import java.math.BigDecimal;

public record ProductCustomizerI18n(
                String id,
                String name,
                BigDecimal price,
                int quantity) {
}
