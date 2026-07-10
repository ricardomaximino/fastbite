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

    public CustomizationOption toDomain() {
        return new CustomizationOption(id, name, price, isSelectedByDefault, defaultValue);
    }

    public static CustomizationOptionDto fromDomain(CustomizationOption domain) {
        if (domain == null) {
            return null;
        }
        return new CustomizationOptionDto(
                domain.getId(),
                domain.getName(),
                domain.getPrice(),
                domain.isSelectedByDefault(),
                domain.getDefaultValue()
        );
    }
}
