package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.application.order.OrderService;
import es.brasatech.fastbite.application.payment.PaymentService;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.domain.order.OrderChannel;
import es.brasatech.fastbite.domain.order.OrderPaymentStatus;
import es.brasatech.fastbite.dto.counter.CounterOrderRequest;
import es.brasatech.fastbite.dto.menu.SequenceNumberServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public String counter(Model model) {
        model.addAttribute("tables", tableService.findAll());
        model.addAttribute("paymentConfig", paymentService.getConfig());
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

        // Use a background order creation to avoid blocking
        orderService.createOrder(
                request.items(),
                orderNumber,
                paymentStatus,
                OrderChannel.COUNTER,
                locale.getLanguage(),
                request.tableId(),
                userDetails.getUsername()); // Find the created order to update table and user info
        // Note: createOrder in OrderService doesn't return the order or take
        // tableId/userId currently
        // Let's modify OrderService.createOrder later or manually update here.
        // For now, I'll update the OrderService to support these fields.

        return Map.of("status", "success", "orderNumber", orderNumber);
    }
}
