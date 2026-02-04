package es.brasatech.fastbite.dto.menu;


import java.util.List;
import java.util.Map;

public record MenuData(Map<String, List<Product>> menuDataMap, List<Customization> customizations, String availableCustomizations, Map<String, String> dictionary, List<Tab> availableTabs, String availableTabsId) {
}
