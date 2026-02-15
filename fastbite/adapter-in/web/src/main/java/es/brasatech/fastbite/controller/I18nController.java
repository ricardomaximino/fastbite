package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.office.CustomizationService;
import es.brasatech.fastbite.application.office.GroupService;
import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.office.ProductService;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.customization.CustomizationI18n;
import es.brasatech.fastbite.domain.customization.CustomizationOptionI18n;
import es.brasatech.fastbite.domain.group.GroupI18n;
import es.brasatech.fastbite.domain.product.ProductI18n;
import es.brasatech.fastbite.domain.table.TableI18n;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Controller for managing translations in BackOffice.
 * Handles translation grid UI and form submissions.
 */
@Controller
@RequestMapping("/backoffice/translations")
@RequiredArgsConstructor
public class I18nController {

        private final GroupService groupService;
        private final ProductService productService;
        private final CustomizationService customizationService;
        private final TableService tableService;
        private final I18nConfig i18NConfig;

        // ===== Group Translations =====

        @GetMapping("/groups/{id}")
        public String showGroupTranslations(@PathVariable String id, Model model) {
                GroupI18n group = groupService.findI18nById(id)
                                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

                model.addAttribute("entityType", "group");
                model.addAttribute("entityId", id);
                model.addAttribute("group", group);
                model.addAttribute("defaultLanguage", i18NConfig.getDefaultLanguage());
                model.addAttribute("availableLocales", i18NConfig.getSupportedLocales());

                return "fastfood/translations";
        }

        @PostMapping("/groups/{id}")
        public String saveGroupTranslations(
                        @PathVariable String id,
                        @RequestParam Map<String, String> formData,
                        RedirectAttributes redirectAttributes) {

                GroupI18n existingGroup = groupService.findI18nById(id)
                                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

                // Parse form data: field_locale format (e.g., "name_en", "description_pt")
                I18nField updatedName = parseFieldTranslations(existingGroup.name(), formData, "name");
                I18nField updatedDescription = parseFieldTranslations(existingGroup.description(), formData,
                                "description");

                GroupI18n updated = new GroupI18n(
                                id,
                                updatedName,
                                updatedDescription,
                                existingGroup.icon(),
                                existingGroup.products());

                groupService.updateI18n(id, updated);

                redirectAttributes.addFlashAttribute("message", "Translations saved successfully!");
                return "redirect:/backoffice";
        }

        // ===== Product Translations =====

        @GetMapping("/products/{id}")
        public String showProductTranslations(@PathVariable String id, Model model) {
                ProductI18n productI18n = productService.findI18nById(id)
                                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

                model.addAttribute("entityType", "product");
                model.addAttribute("entityId", id);
                model.addAttribute("product", productI18n);
                model.addAttribute("defaultLanguage", i18NConfig.getDefaultLanguage());
                model.addAttribute("availableLocales", i18NConfig.getSupportedLocales());

                return "fastfood/translations";
        }

        @PostMapping("/products/{id}")
        public String saveProductTranslations(
                        @PathVariable String id,
                        @RequestParam Map<String, String> formData,
                        RedirectAttributes redirectAttributes) {

                ProductI18n existingProduct = productService.findI18nById(id)
                                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

                I18nField updatedName = parseFieldTranslations(existingProduct.name(), formData, "name");
                I18nField updatedDescription = parseFieldTranslations(existingProduct.description(), formData,
                                "description");

                ProductI18n updated = new ProductI18n(
                                id,
                                updatedName,
                                existingProduct.price(),
                                updatedDescription,
                                existingProduct.image(),
                                existingProduct.customizations(),
                                existingProduct.active());

                productService.updateI18n(id, updated);

                redirectAttributes.addFlashAttribute("message", "Translations saved successfully!");
                return "redirect:/backoffice";
        }

        // ===== Customization Translations =====

        @GetMapping("/customizations/{id}")
        public String showCustomizationTranslations(@PathVariable String id, Model model) {
                CustomizationI18n customizationI18n = customizationService.findI18nById(id)
                                .orElseThrow(() -> new RuntimeException("Customization not found: " + id));

                model.addAttribute("entityType", "customization");
                model.addAttribute("entityId", id);
                model.addAttribute("customization", customizationI18n);
                model.addAttribute("defaultLanguage", i18NConfig.getDefaultLanguage());
                model.addAttribute("availableLocales", i18NConfig.getSupportedLocales());

                return "fastfood/translations";
        }

        @PostMapping("/customizations/{id}")
        public String saveCustomizationTranslations(
                        @PathVariable String id,
                        @RequestParam Map<String, String> formData,
                        RedirectAttributes redirectAttributes) {

                CustomizationI18n existingCustomization = customizationService.findI18nById(id)
                                .orElseThrow(() -> new RuntimeException("Customization not found: " + id));

                I18nField updatedName = parseFieldTranslations(existingCustomization.name(), formData, "name");

                // Parse option translations using option ID
                var updatedOptions = existingCustomization.options().stream()
                                .map(opt -> {
                                        I18nField updatedOptionName = parseFieldTranslations(opt.name(), formData,
                                                        "option_" + opt.id());
                                        return new CustomizationOptionI18n(
                                                        opt.id(),
                                                        updatedOptionName,
                                                        opt.price(),
                                                        opt.isSelectedByDefault(),
                                                        opt.defaultValue());
                                })
                                .toList();

                CustomizationI18n updated = new CustomizationI18n(
                                id,
                                updatedName,
                                existingCustomization.type(),
                                updatedOptions,
                                existingCustomization.usageCount());

                customizationService.updateI18n(id, updated);

                redirectAttributes.addFlashAttribute("message", "Translations saved successfully!");
                return "redirect:/backoffice";
        }

        // ===== Table Translations =====

        @GetMapping("/tables/{id}")
        public String showTableTranslations(@PathVariable String id, Model model) {
                TableI18n tableI18n = tableService.findI18nById(id)
                                .orElseThrow(() -> new RuntimeException("Table not found: " + id));

                model.addAttribute("entityType", "table");
                model.addAttribute("entityId", id);
                model.addAttribute("table", tableI18n);
                model.addAttribute("defaultLanguage", i18NConfig.getDefaultLanguage());
                model.addAttribute("availableLocales", i18NConfig.getSupportedLocales());

                return "fastfood/translations";
        }

        @PostMapping("/tables/{id}")
        public String saveTableTranslations(
                        @PathVariable String id,
                        @RequestParam Map<String, String> formData,
                        RedirectAttributes redirectAttributes) {

                TableI18n existingTable = tableService.findI18nById(id)
                                .orElseThrow(() -> new RuntimeException("Table not found: " + id));

                I18nField updatedName = parseFieldTranslations(existingTable.name(), formData, "name");

                TableI18n updated = new TableI18n(
                                id,
                                updatedName,
                                existingTable.seats(),
                                existingTable.status(),
                                existingTable.active());

                tableService.updateI18n(id, updated);

                redirectAttributes.addFlashAttribute("message", "Translations saved successfully!");
                return "redirect:/backoffice";
        }

        // ===== Helper Methods =====

        /**
         * Parse form data for a specific field across all locales.
         * Form fields are named: fieldName_locale (e.g., "name_en", "description_pt")
         */
        private I18nField parseFieldTranslations(I18nField existing, Map<String, String> formData, String fieldName) {
                I18nField updated = new I18nField(existing.getAll());

                // Look for all form parameters matching fieldName_*
                formData.forEach((key, value) -> {
                        if (key.startsWith(fieldName + "_")) {
                                String locale = key.substring(fieldName.length() + 1);
                                updated.set(locale, value);
                        }
                });

                return updated;
        }
}
