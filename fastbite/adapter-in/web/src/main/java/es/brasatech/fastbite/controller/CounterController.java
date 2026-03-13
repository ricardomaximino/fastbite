package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.office.CustomizationService;
import es.brasatech.fastbite.application.office.GroupService;
import es.brasatech.fastbite.application.office.ProductService;
import es.brasatech.fastbite.application.order.OrderService;
import es.brasatech.fastbite.application.payment.PaymentService;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.domain.order.OrderChannel;
import es.brasatech.fastbite.domain.order.OrderPaymentStatus;
import es.brasatech.fastbite.domain.table.TableStatus;
import es.brasatech.fastbite.domain.user.Customer;
import es.brasatech.fastbite.dto.counter.CounterOrderRequest;
import es.brasatech.fastbite.dto.menu.SequenceNumberServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/counter")
@RequiredArgsConstructor
public class CounterController {

    private final TableService tableService;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final SequenceNumberServiceImpl sequenceNumberService;
    private final GroupService groupService;
    private final ProductService productService;
    private final CustomizationService customizationService;

    @GetMapping
    public String counter(Model model) {
        model.addAttribute("tables", tableService.findAll());
        model.addAttribute("paymentConfig", paymentService.getActiveConfig());
        return "fastfood/counter";
    }

    @ResponseBody
    @PostMapping("/api/order")
    public Map<String, Object> createOrder(
            @RequestBody CounterOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            Locale locale) {

        var orderNumber = sequenceNumberService.getNextSequenceNumber();
        var paymentStatus = request.paid() ? OrderPaymentStatus.PAID : OrderPaymentStatus.UNPAID;

        Order order = orderService.createOrder(
                request.items(),
                orderNumber,
                paymentStatus,
                OrderChannel.COUNTER,
                locale.getLanguage(),
                userDetails.getUsername());

        if (request.tableId() != null && !request.tableId().isEmpty()) {
            tableService.assignOrder(request.tableId(), order.id());
        }

        return Map.of("status", "success", "orderNumber", orderNumber, "orderId", order.id());
    }

    @ResponseBody
    @GetMapping("/api/tables/{tableId}/active-orders")
    public List<Order> getActiveOrders(@PathVariable String tableId) {
        return orderService.findActiveByTableId(tableId);
    }

    @ResponseBody
    @PostMapping("/api/tables/{tableId}/status")
    public Map<String, Object> updateTableStatus(@PathVariable String tableId, @RequestParam TableStatus status) {
        orderService.setTableStatus(tableId, status);
        return Map.of("status", "success");
    }

    @ResponseBody
    @PutMapping("/api/orders/{orderId}")
    public Map<String, Object> updateOrder(
            @PathVariable String orderId,
            @RequestBody CounterOrderRequest request) {

        var existingOrder = orderService.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        var updatedOrder = new Order(
                request.items(),
                existingOrder.orderNumber(),
                orderId,
                existingOrder.createdAt(),
                java.time.LocalDateTime.now(),
                existingOrder.status(),
                request.items().stream()
                        .map(item -> item.totalPrice())
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add),
                existingOrder.cancelReason(),
                request.paid() ? OrderPaymentStatus.PAID : existingOrder.paymentStatus(),
                existingOrder.orderChannel(),
                existingOrder.orderLanguage(),
                existingOrder.userId());

        orderService.update(orderId, updatedOrder);
        return Map.of("status", "success", "orderId", orderId);
    }

    @ResponseBody
    @PostMapping("/api/orders/{orderId}/cancel")
    public Map<String, Object> cancelOrder(@PathVariable String orderId,
            @RequestParam(required = false) String reason) {
        orderService.cancelOrder(orderId, reason != null ? reason : "Canceled at Counter");
        return Map.of("status", "success");
    }

    @ResponseBody
    @PostMapping("/api/orders/{orderId}/reassign")
    public Map<String, Object> reassignOrder(@PathVariable String orderId, @RequestParam String tableId) {
        tableService.findTableByOrderId(orderId).ifPresent(table -> {
            tableService.unassignOrder(table.id(), orderId);
        });

        tableService.assignOrder(tableId, orderId);
        return Map.of("status", "success");
    }

    // ===== Fragment Endpoints =====

    @GetMapping("/fragments/categories")
    public String getCategoriesFragment(Model model,
            @RequestParam(required = false, defaultValue = "all") String selectedGroup) {
        model.addAttribute("categories", groupService.findAll());
        model.addAttribute("selectedGroup", selectedGroup);
        return "fastfood/fragments/counter :: categories";
    }

    @GetMapping("/fragments/products")
    public String getProductsFragment(Model model,
            @RequestParam(required = false, defaultValue = "all") String groupId,
            @RequestParam(required = false, defaultValue = "") String filter) {

        var products = productService.findAll().stream()
                .filter(p -> {
                    boolean matchesFilter = p.name().toLowerCase().contains(filter.toLowerCase());
                    boolean matchesGroup = "all".equals(groupId);
                    if (!matchesGroup) {
                        matchesGroup = groupService.findById(groupId)
                                .map(g -> g.products() != null && g.products().contains(p.id()))
                                .orElse(false);
                    }
                    return matchesFilter && matchesGroup;
                })
                .toList();

        model.addAttribute("products", products);
        return "fastfood/fragments/counter :: products";
    }

    @GetMapping("/fragments/tables")
    public String getTablesFragment(Model model, @RequestParam(required = false) String selectedTableId) {
        model.addAttribute("tables", tableService.findAll());
        model.addAttribute("selectedTableId", selectedTableId);
        return "fastfood/fragments/counter :: tables";
    }

    @GetMapping("/fragments/reassign-tables")
    public String getReassignTablesFragment(Model model, @RequestParam(required = false) String currentTableId) {
        model.addAttribute("tables", tableService.findAll());
        model.addAttribute("currentTableId", currentTableId);
        return "fastfood/fragments/counter :: reassign-tables";
    }

    @GetMapping("/fragments/active-orders/{tableId}")
    public String getActiveOrdersFragment(Model model, @PathVariable String tableId) {
        var orders = orderService.findActiveByTableId(tableId);
        var enriched = orders.stream().map(o -> Map.of(
                "id", o.id(),
                "orderNumber", o.orderNumber(),
                "total", o.total(),
                "items", o.items(),
                "createdAt", o.createdAt())).toList();
        model.addAttribute("orders", enriched);
        return "fastfood/fragments/counter :: active-orders";
    }

    @PostMapping("/fragments/order-cart")
    public String getOrderCartFragment(Model model, @RequestBody List<CartItem> items) {
        // Enriched list for the fragment
        var enrichedItems = items.stream().map(item -> {
            boolean customizable = productService.findById(item.itemId())
                    .map(p -> p.customizations() != null && !p.customizations().isEmpty())
                    .orElse(false);

            return Map.of(
                    "name", item.name(),
                    "price", item.price(),
                    "quantity", item.quantity(),
                    "customizations", item.customizations(),
                    "customizable", customizable);
        }).toList();

        model.addAttribute("items", enrichedItems);
        return "fastfood/fragments/counter :: order-cart";
    }

    @GetMapping("/fragments/table-session-cart/{tableId}")
    public String getTableSessionCartFragment(Model model, @PathVariable String tableId,
            @RequestParam(required = false) List<Integer> expandedIndices) {
        var orders = orderService.findActiveByTableId(tableId);

        // Wrap orders to include expansion state and simplify data for fragment
        var wrappedOrders = new java.util.ArrayList<Map<String, Object>>();
        for (int i = 0; i < orders.size(); i++) {
            var order = orders.get(i);

            var itemMaps = order.items().stream().map(item -> {
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("name", item.name());
                m.put("quantity", item.quantity());
                m.put("totalPrice", item.totalPrice());
                m.put("customizations", item.customizations());
                return m;
            }).toList();

            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", order.id());
            map.put("orderNumber", order.orderNumber());
            map.put("total", order.total());
            map.put("status", order.status() != null ? order.status().name() : "CREATED");
            map.put("paymentStatus", order.paymentStatus() != null ? order.paymentStatus().name() : "UNPAID");
            map.put("items", itemMaps);
            map.put("expanded", expandedIndices != null && expandedIndices.contains(i));

            wrappedOrders.add(map);
        }

        model.addAttribute("orders", wrappedOrders);
        return "fastfood/fragments/counter :: table-session-cart";
    }

    @GetMapping("/fragments/customization-options/{productId}")
    public String getCustomizationOptionsFragment(Model model,
            @PathVariable String productId,
            @RequestParam(required = false) List<String> selected) {

        var product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        var customizations = product.customizations().stream()
                .map(id -> customizationService.findById(id).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();

        model.addAttribute("customizations", customizations);
        model.addAttribute("selectedOptions", selected != null ? selected : List.of());
        return "fastfood/fragments/counter :: customization-options";
    }

    @GetMapping("/receipt")
    public String getReceipt(
            @RequestParam("ids") List<String> orderIds,
            @RequestParam(required = false, defaultValue = "false") boolean isInvoice,
            @RequestParam(required = false, defaultValue = "false") boolean isProforma,
            @RequestParam(required = false) String taxId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String address,
            Model model) {

        var orders = orderIds.stream()
                .map(id -> orderService.findById(id).orElseThrow(() -> new RuntimeException("Order not found: " + id)))
                .toList();

        model.addAttribute("orders", orders);
        model.addAttribute("isInvoice", isInvoice);
        model.addAttribute("isProforma", isProforma);

        if (isInvoice) {
            model.addAttribute("customer", new Customer(taxId, customerName, address));
        }

        return "fastfood/receipt";
    }
}
