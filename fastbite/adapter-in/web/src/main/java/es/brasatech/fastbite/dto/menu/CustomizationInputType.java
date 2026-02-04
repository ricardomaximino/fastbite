package es.brasatech.fastbite.dto.menu;

public enum CustomizationInputType {
    RADIO("radio"),
    CHECKBOX("checkbox"),
    HIDDEN("hidden");

    private final String value;

    CustomizationInputType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
