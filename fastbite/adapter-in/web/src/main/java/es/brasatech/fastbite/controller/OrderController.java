package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.order.OrderNumberService;
import es.brasatech.fastbite.application.order.OrderPricingService;
import es.brasatech.fastbite.application.order.OrderService;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.config.TenantRoutingResolver;
import es.brasatech.fastbite.domain.order.OrderChannel;
import es.brasatech.fastbite.domain.order.OrderPaymentStatus;
import es.brasatech.fastbite.domain.order.ServiceType;
import es.brasatech.fastbite.domain.table.Table;
import es.brasatech.fastbite.dto.order.OrderCancelReason;
import es.brasatech.fastbite.dto.order.OrderStatusChange;
import es.brasatech.fastbite.service.OrderCheckoutService;
import jakarta.servlet.http.HttpServletRequest;
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

import es.brasatech.fastbite.application.settings.RestaurantSettingsService;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private static final String SESSION_ORDER_ID = "orderId";

    private final MessageSource messageSource;
    private final OrderNumberService orderNumberService;
    private final OrderPricingService orderPricingService;
    private final OrderService orderService;
    private final TableService tableService;
    private final RestaurantSettingsService settingsService;
    private final OrderCheckoutService orderCheckoutService;

    public record CreateOrderRequest(
        List<CartItem> items,
        String customerName,
        String tableNumber,
        String paymentMethod,
        String serviceType
    ) {}

    @ResponseBody
    @PostMapping({"/{tenantId}/api/create-order", "/api/create-order"})
    public Map<String, Object> postOrder(@RequestBody CreateOrderRequest request, Locale locale, HttpSession session) {
        try {
            // A guest whose QR code bound this session to a table orders for that table, whatever the form says
            String boundTable = (String) session.getAttribute("tableNumber");
            boolean takeaway = boundTable == null && ServiceType.TAKEAWAY.name().equalsIgnoreCase(request.serviceType());
            Order order = takeaway ? takeawayOrder(request, locale) : tableOrder(request, boundTable, locale);

            session.setAttribute("cart", order.items());
            session.setAttribute("orderNumber", order.orderNumber());
            session.setAttribute(SESSION_ORDER_ID, order.id());

            return Map.of("status", "success", "payFirst", order.heldUntilPaid());
        } catch (IllegalArgumentException e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    private Order tableOrder(CreateOrderRequest request, String boundTable, Locale locale) {
        if (!settingsService.get().dineIn()) {
            throw new IllegalArgumentException("This restaurant is not taking table orders");
        }
        List<CartItem> items = pricedItems(request, OrderChannel.TABLE);
        String table = boundTable != null ? boundTable : request.tableNumber();
        String tableId = tableService.findTableByNameOrId(table).map(Table::id)
                .orElseThrow(() -> new IllegalArgumentException("Table does not exist"));
        return orderService.createOrderForTable(items, orderNumberService.next(), tableId, locale.getLanguage(),
                request.customerName());
    }

    /** Saved now, but held back from the kitchen until the guest has paid online. */
    private Order takeawayOrder(CreateOrderRequest request, Locale locale) {
        if (!settingsService.get().takeaway() || !orderCheckoutService.isAvailable()) {
            throw new IllegalArgumentException("This restaurant is not taking takeaway orders online");
        }
        if (request.customerName() == null || request.customerName().isBlank()) {
            throw new IllegalArgumentException("Tell us your name so we can call you when the order is ready");
        }
        List<CartItem> items = pricedItems(request, OrderChannel.ONLINE);
        return orderService.createOrder(items, orderNumberService.next(), OrderPaymentStatus.UNPAID, OrderChannel.ONLINE,
                locale.getLanguage(), null, request.customerName().trim(), ServiceType.TAKEAWAY);
    }

    private List<CartItem> pricedItems(CreateOrderRequest request, OrderChannel channel) {
        List<CartItem> items = orderPricingService.price(request.items(), channel);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty");
        }
        return items;
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
    public String confirmation(@RequestParam(name = "session_id", required = false) String checkoutSessionId,
            HttpSession session, HttpServletRequest request, Model model) {
        var orderNumber = session.getAttribute("orderNumber");
        model.addAttribute("orderNumber", orderNumber);
        if (checkoutSessionId != null) {
            // Back from Stripe: ask Stripe whether the payment went through
            model.addAttribute("paidOnline", orderCheckoutService.confirmReturn(checkoutSessionId));
        }
        boolean stillToPay = Optional.ofNullable((String) session.getAttribute(SESSION_ORDER_ID))
                .flatMap(orderService::findById)
                .filter(Order::heldUntilPaid)
                .isPresent();
        if (stillToPay) {
            // Nothing is being prepared yet, so there is nothing to confirm: back to the payment page
            Object prefix = request.getAttribute(TenantRoutingResolver.TENANT_URL_PREFIX);
            return "redirect:" + (prefix != null ? prefix : "") + "/select-payment";
        }
        return "fastfood/confirmation";
    }

    @GetMapping({"/{tenantId}/dashboard", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("settings", settingsService.get());
        return "fastfood/dashboard";
    }
}
