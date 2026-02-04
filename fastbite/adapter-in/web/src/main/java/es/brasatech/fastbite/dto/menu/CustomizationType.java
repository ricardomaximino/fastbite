package es.brasatech.fastbite.dto.menu;

import lombok.Getter;

@Getter
public enum CustomizationType {
    SIZE("customizer.type.size"),
    ADD_EXTRA("customizer.type.add.extra"),
    TOGGLE_ITEMS("customizer.type.toggle.items"),
    DRINKS("customizer.type.drinks"),
    MEATS("customizer.type.meats");

    private final String key;

    CustomizationType(String key) {
        this.key = key;
    }
}
