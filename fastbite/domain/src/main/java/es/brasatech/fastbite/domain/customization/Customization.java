package es.brasatech.fastbite.domain.customization;

import java.util.List;

/**
 * Domain model representing a Customization.
 * Pure business logic representation decoupled from transport DTOs.
 */
public class Customization {
    private final String id;
    private final String name;
    private final String type;
    private final List<CustomizationOption> options;
    private final int usageCount;

    public Customization(String id, String name, String type, List<CustomizationOption> options, int usageCount) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.options = options != null ? List.copyOf(options) : List.of();
        this.usageCount = usageCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<CustomizationOption> getOptions() {
        return options;
    }

    public int getUsageCount() {
        return usageCount;
    }
}
