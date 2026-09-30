package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.order.OrderNumberService;
import es.brasatech.fastbite.application.order.OrderService;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.dto.order.OrderCancelReason;
import es.brasatech.fastbite.dto.order.OrderStatusChange;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import es.brasatech.fastbite.application.kds.KdsConfigService;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private static final String SESSION_ORDER_ID = "orderId";

    private final MessageSource messageSource;
    private final OrderNumberService orderNumberService;
    private final OrderService orderService;
    private final TableService tableService;
    private final KdsConfigService kdsConfigService;

    public record CreateOrderRequest(
        List<CartItem> items,
        String customerName,
        String tableNumber,
        String paymentMethod
    ) {}

    @ResponseBody
    @PostMapping({"/{tenantId}/api/create-order", "/api/create-order"})
    public Map<String, Object> postOrder(@RequestBody CreateOrderRequest request, Locale locale, HttpSession session) {
        var orderNumber = orderNumberService.next();
        
        try {
            Order order = orderService.createOrderForTable(
                request.items(),
                orderNumber,
                request.tableNumber(),
                locale.getLanguage(),
                request.customerName()
            );

            session.setAttribute("cart", request.items());
            session.setAttribute("orderNumber", orderNumber);
            session.setAttribute(SESSION_ORDER_ID, order.id());

            return Map.of("status", "success");
        } catch (IllegalArgumentException e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    /** Status of the order this guest placed; guests can only ever see their own order. */
    @ResponseBody
    @GetMapping({"/{tenantId}/api/order-status", "/api/order-status"})
    public ResponseEntity<Map<String, String>> orderStatus(HttpSession session) {
        return Optional.ofNullable((String) session.getAttribute(SESSION_ORDER_ID))
                .flatMap(orderService::findById)
                .map(order -> ResponseEntity.ok(Map.of("status", order.status().name())))
                .orElse(ResponseEntity.notFound().build());
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

    @ResponseBody
    @PostMapping("/api/backoffice/orders/reassign-table")
    public Map<String, Object> reassignTable(@RequestParam String orderId, @RequestParam String tableId) {
        var table = tableService.findTableByOrderId(orderId);
        table.ifPresent(value -> tableService.unassignOrder(value.id(), orderId));
        tableService.assignOrder(tableId, orderId);
        return Map.of("status", "success");
    }

    @GetMapping({"/{tenantId}/order-confirmation", "/order-confirmation"})
    public String confirmation(HttpSession session, Model model) {
        var orderNumber = session.getAttribute("orderNumber");
        model.addAttribute("orderNumber", orderNumber);
        return "fastfood/confirmation";
    }

    @GetMapping({"/{tenantId}/dashboard", "/dashboard"})
    public String dashboard(@PathVariable(required = false) String tenantId, HttpSession session, Model model) {
        String activeTenant = (tenantId != null) ? tenantId : "kebab";
        var kdsConfig = kdsConfigService.getKdsConfig(activeTenant);
        model.addAttribute("kdsConfig", kdsConfig);
        return "fastfood/dashboard";
    }
}
