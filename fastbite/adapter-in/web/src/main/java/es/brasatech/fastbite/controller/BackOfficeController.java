package es.brasatech.fastbite.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BackOfficeController {

    /**
     * Render the BackOffice page
     */
    @GetMapping("/backoffice")
    public String backOffice() {
        return "fastfood/backOffice";
    }

    @PostMapping("/api/backoffice/fragments/groups-list")
    public String getGroupsList(@RequestBody List<Map<String, Object>> groups, Model model) {
        model.addAttribute("groups", new java.util.ArrayList<>(groups));
        return "fastfood/fragments/backOffice :: groups-list";
    }

    @PostMapping("/api/backoffice/fragments/customizations-list")
    public String getCustomizationsList(@RequestBody List<Map<String, Object>> customizations, Model model) {
        model.addAttribute("customizations", new java.util.ArrayList<>(customizations));
        return "fastfood/fragments/backOffice :: customizations-list";
    }

    @PostMapping("/api/backoffice/fragments/products-list")
    public String getProductsList(@RequestBody List<Map<String, Object>> products, Model model) {
        model.addAttribute("products", new java.util.ArrayList<>(products));
        return "fastfood/fragments/backOffice :: products-list";
    }

    @PostMapping("/api/backoffice/fragments/customization-option")
    public String getCustomizationOption(@RequestBody Map<String, Object> payload, Model model) {
        model.addAttribute("opt", payload.get("opt"));
        model.addAttribute("index", payload.get("index"));
        return "fastfood/fragments/backOffice :: customization-option";
    }

    @PostMapping("/api/backoffice/fragments/image-gallery")
    public String getImageGallery(@RequestBody Map<String, Object> payload, Model model) {
        model.addAttribute("imagesByFolder", payload.get("imagesByFolder"));
        model.addAttribute("type", payload.get("type"));
        return "fastfood/fragments/backOffice :: image-gallery";
    }

    @PostMapping("/api/backoffice/fragments/quick-view")
    public String getQuickView(@RequestBody Map<String, Object> payload, Model model) {
        model.addAttribute("data", payload.get("data"));
        model.addAttribute("type", payload.get("type"));
        return "fastfood/fragments/backOffice :: quick-view";
    }

    @PostMapping("/api/backoffice/fragments/group-products-badges")
    public String getGroupProductsBadges(@RequestBody List<Map<String, Object>> selectedProducts, Model model) {
        model.addAttribute("selectedProducts", selectedProducts);
        return "fastfood/fragments/backOffice :: group-products-badges";
    }

    @PostMapping("/api/backoffice/fragments/product-customizations-badges")
    public String getProductCustomizationsBadges(@RequestBody List<Map<String, Object>> selectedCustomizations,
            Model model) {
        model.addAttribute("selectedCustomizations", selectedCustomizations);
        return "fastfood/fragments/backOffice :: product-customizations-badges";
    }

    @PostMapping("/api/backoffice/fragments/selector-list")
    public String getSelectorList(@RequestBody Map<String, Object> payload, Model model) {
        model.addAttribute("items", payload.get("items"));
        model.addAttribute("selectedIds", payload.get("selectedIds"));
        return "fastfood/fragments/backOffice :: selector-list";
    }

    @PostMapping("/api/backoffice/fragments/tables-list")
    public String getTablesList(@RequestBody List<Map<String, Object>> tables, Model model) {
        model.addAttribute("tables", new java.util.ArrayList<>(tables));
        return "fastfood/fragments/backOffice :: tables-list";
    }

    @PostMapping("/api/backoffice/fragments/table-form")
    public String getTableForm(@RequestBody Map<String, Object> payload, Model model) {
        model.addAttribute("table", payload.get("table"));
        model.addAttribute("statuses", payload.get("statuses"));
        return "fastfood/fragments/backOffice :: table-form";
    }

    @PostMapping("/api/backoffice/fragments/payment-section")
    public String getPaymentSection(@RequestBody Map<String, Object> payload, Model model) {
        model.addAttribute("paymentConfig", payload.get("paymentConfig"));
        model.addAttribute("allConfigs", payload.get("allConfigs"));
        return "fastfood/fragments/backOffice :: payment-section";
    }

}
