package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.dto.menu.MenuData;
import es.brasatech.fastbite.dto.menu.OrderDto;
import es.brasatech.fastbite.dto.office.MenuDataService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MenuController {

    private final MenuDataService menuDataService;
    private final double taxPercentage = 10;

    @GetMapping(value = { "/", "/menu" })
    public String sample2(Model model) {
        return "fastfood/menu";
    }

    @PostMapping("/api/calculate-cart")
    public String calculateCart(@RequestBody List<CartItem> cartItems, Model model) {
        calculate(cartItems, model);
        return "fastfood/fragments/menu :: #cart";
    }

    @PostMapping("/api/calculate-confirmation")
    public String calculateConfirmation(@RequestBody List<CartItem> cartItems, Model model) {
        calculate(cartItems, model);
        return "fastfood/fragments/menu :: #confirmation";
    }

    @PostMapping("/api/toast")
    public String getToast(@RequestBody Map<String, String> payload, Model model) {
        model.addAttribute("message", payload.get("message"));
        return "fastfood/fragments/menu :: toast";
    }

    @GetMapping("/select-payment")
    @SuppressWarnings("unchecked")
    public String selectPayment(HttpSession session, Model model) {
        var orderNumber = (String) session.getAttribute("orderNumber");
        var cartItems = (List<CartItem>) session.getAttribute("cart");
        var order = new OrderDto(orderNumber, cartItems != null ? cartItems : new ArrayList<>());
        model.addAttribute("order", order);
        return "fastfood/paymentSelection";
    }

    /**
     * Provides MenuData for all controller methods.
     * Can use either BackOffice data or hardcoded I18nHelper data based on
     * configuration.
     */
    @ModelAttribute
    private MenuData menu(Locale locale) {
        return menuDataService.buildMenuData(locale);
    }

    private void calculate(List<CartItem> cartItems, Model model) {
        var total = cartItems.stream().map(CartItem::totalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = total.multiply(BigDecimal.valueOf(taxPercentage / 100)).setScale(2, RoundingMode.CEILING);
        model.addAttribute("cart", cartItems);
        model.addAttribute("subtotal", total);
        model.addAttribute("tax", tax);
        model.addAttribute("total", total);
    }
}
