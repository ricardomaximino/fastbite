package es.brasatech.fastbite.dto.menu;

import java.math.BigDecimal;

public record CustomizationOption(String id, String name, BigDecimal price, boolean isSelectedByDefault, int defaultValue) {
}
