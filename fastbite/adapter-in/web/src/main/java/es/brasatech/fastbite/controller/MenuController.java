package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.dto.menu.MenuData;
import es.brasatech.fastbite.dto.menu.OrderDto;
import es.brasatech.fastbite.dto.office.MenuDataService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    private final TableService tableService;
    
    @Value("${fastbite.tax.percentage:0.0}")
    private double taxPercentage;

    @GetMapping(value = { "/", "/menu" })
    @SuppressWarnings("unchecked")
    public String menu(
            @RequestParam(value = "table", required = false) String tableParam,
            @RequestParam(value = "token", required = false) String tokenParam,
            HttpSession session,
            Model model) {
        
        try {
            String existingTableNumber = (String) session.getAttribute("tableNumber");
            List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cart");
            String boundTableId = tableService.validateAndBindTableSession(tableParam, tokenParam, existingTableNumber, cartItems);
            
            if (boundTableId != null) {
                var tableOpt = tableService.findById(boundTableId);
                tableOpt.ifPresent(table -> {
                    session.setAttribute("tableNumber", table.id());
                    session.setAttribute("tableName", table.name());
                });
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
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
        var orderNumberObj = session.getAttribute("orderNumber");
        var orderNumber = orderNumberObj != null ? orderNumberObj.toString() : null;
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
