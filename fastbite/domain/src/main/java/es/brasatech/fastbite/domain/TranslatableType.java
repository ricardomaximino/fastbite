package es.brasatech.fastbite.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * Kinds of items whose texts can be translated, with the path segment used in their URLs.
 */
public enum TranslatableType {
    GROUP("groups", "Group"),
    PRODUCT("products", "Product"),
    CUSTOMIZATION("customizations", "Customization"),
    TABLE("tables", "Table"),
    DISCOUNT("discounts", "Discount");

    private final String path;
    private final String label;

    TranslatableType(String path, String label) {
        this.path = path;
        this.label = label;
    }

    public String path() {
        return path;
    }

    public String label() {
        return label;
    }

    public static Optional<TranslatableType> fromPath(String path) {
        return Arrays.stream(values()).filter(type -> type.path.equals(path)).findFirst();
    }
}
