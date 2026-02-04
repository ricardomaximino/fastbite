package es.brasatech.fastbite.dto.menu;

import java.util.List;

public record Customization(String id, String name, CustomizationInputType inputType, List<CustomizationOption> options) {
}
