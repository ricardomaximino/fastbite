package es.brasatech.fastbite.domain.customization;

import java.math.BigDecimal;

/**
 * Domain model representing a Customization Option.
 * Pure business logic representation decoupled from transport DTOs.
 */
public class CustomizationOption {
    private final String id;
    private final String name;
    private final BigDecimal price;
    private final boolean selectedByDefault;
    private final int defaultValue;

    public CustomizationOption(String id, String name, BigDecimal price, boolean selectedByDefault, int defaultValue) {
        this.id = id;
        this.name = name;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.selectedByDefault = selectedByDefault;
        this.defaultValue = defaultValue;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isSelectedByDefault() {
        return selectedByDefault;
    }

    public int getDefaultValue() {
        return defaultValue;
    }
}
