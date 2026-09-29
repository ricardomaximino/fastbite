package es.brasatech.fastbite.domain.customization;

import java.math.BigDecimal;

/**
 * DTO for Customization Option in BackOffice.
 */
public record CustomizationOptionDto(
        String id,
        String name,
        BigDecimal price,
        boolean isSelectedByDefault,
        int defaultValue) {
    /**
     * Constructor with default values
     */
    public CustomizationOptionDto(String id, String name, BigDecimal price) {
        this(id, name, price, false, 1);
    }
}
