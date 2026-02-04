package es.brasatech.fastbite.domain.product;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProductCustomizer(String id, String name, BigDecimal price, int quantity) implements Serializable {
}
