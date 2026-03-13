package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.order.OrderService;
import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.domain.order.OrderChannel;
import es.brasatech.fastbite.domain.order.OrderPaymentStatus;
import es.brasatech.fastbite.dto.menu.SequenceNumberServiceImpl;
import es.brasatech.fastbite.dto.order.OrderCancelReason;
import es.brasatech.fastbite.dto.order.OrderStatusChange;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final MessageSource messageSource;
    private final SequenceNumberServiceImpl sequenceNumberService;
    private final OrderService orderService;


    @ResponseBody
    @PostMapping("/api/create-order")
    public Map<String, Object> postOrder(@RequestBody List<CartItem> cartItems, Locale locale, HttpSession session) {
        var orderNumber = sequenceNumberService.getNextSequenceNumber();
        orderService.createOrder(cartItems, orderNumber, OrderPaymentStatus.UNPAID, OrderChannel.ONLINE, locale.getLanguage());
        session.setAttribute("cart", cartItems);
        session.setAttribute("orderNumber", orderNumber);

        return Map.of("status", "success");
    }

    @ResponseBody
    @PostMapping("/api/order")
    public List<Order> postOrder() {
        return orderService.getAllOrder();
    }

    @ResponseBody
    @PostMapping("/api/order/{id}/next")
    public void nextOrderStatus(@PathVariable String id) {
        orderService.moveToNextStatus(id);
    }

    @ResponseBody
    @PostMapping("/api/order/{id}/previous")
    public void previousOrderStatus(@PathVariable String id) {
        orderService.moveToPreviousStatus(id);
    }

    @ResponseBody
    @PostMapping("/api/order/batch/next")
    public void batchNextStatus(@RequestBody List<String> ids) {
        orderService.batchMoveStatus(ids, true);
    }

    @ResponseBody
    @PostMapping("/api/order/batch/previous")
    public void batchPreviousStatus(@RequestBody List<String> ids) {
        orderService.batchMoveStatus(ids, false);
    }

    @ResponseBody
    @PostMapping("/api/order/{id}/cancel")
    public void cancelOrder(@PathVariable String id, @RequestBody OrderCancelReason orderCancelReason) {
        orderService.cancelOrder(id, orderCancelReason.value());
    }

    @ResponseBody
    @PostMapping("/api/order/{id}/status")
    public void changeStatus(@PathVariable String id, @RequestBody OrderStatusChange orderStatusChange) {
        orderService.setOrderStatus(id, orderStatusChange.value());
    }

    @GetMapping("/order-confirmation")
    public String confirmation(HttpSession session, Model model) {
        var orderNumber = session.getAttribute("orderNumber");
        model.addAttribute("orderNumber", orderNumber);
        return "fastfood/confirmation";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        return "fastfood/dashboard";
    }
}
