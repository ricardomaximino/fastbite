package es.brasatech.fastbite.controller;

import com.stripe.model.checkout.Session;
import es.brasatech.fastbite.application.order.OrderService;
import es.brasatech.fastbite.application.tenant.TenantLocationService;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.domain.tenant.TenantContext;
import es.brasatech.fastbite.domain.tenant.TenantLocation;
import es.brasatech.fastbite.service.StripeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StripeCheckoutController {

    private final StripeService stripeService;
    private final TenantLocationService tenantLocationService;
    private final OrderService orderService;

    @Value("${fastbite.protocol:http}")
    private String protocol;

    public record CreateCheckoutRequest(
            String tenantId,
            String orderId,
            BigDecimal amount,
            String customerName
    ) {}

    /**
     * Level 2: Create Stripe Checkout Session for guest customer food orders at a restaurant location.
     */
    @PostMapping("/api/stripe/create-checkout-session")
    @ResponseBody
    public ResponseEntity<?> createRestaurantCheckoutSession(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest httpRequest) {

        String rawTenantId = payload.get("tenantId") != null ? payload.get("tenantId").toString() : null;
        String orderId = payload.get("orderId") != null ? payload.get("orderId").toString() : null;
        String customerName = payload.get("customerName") != null ? payload.get("customerName").toString() : null;
        BigDecimal amount = null;
        if (payload.get("amount") != null && !payload.get("amount").toString().isBlank()) {
            try {
                amount = new BigDecimal(payload.get("amount").toString());
            } catch (Exception ignored) {}
        }

        log.info("Creating Stripe Checkout Session for restaurant order: {} in tenant: {}", orderId, rawTenantId);
        try {
            String tenantId = rawTenantId;
            if (tenantId == null || tenantId.isBlank()) {
                tenantId = TenantContext.getCurrentTenant();
            }
            if (tenantId == null || tenantId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tenant ID is required"));
            }

            // Determine Stripe Connect Account ID for this location if configured
            Optional<TenantLocation> locationOpt = tenantLocationService.getLocation(tenantId);
            String stripeAccountId = locationOpt.map(TenantLocation::stripeAccountId).orElse(null);

            // Attempt to look up order details if orderId is provided
            if (orderId != null && !orderId.isBlank()) {
                try {
                    TenantContext.setCurrentTenant(tenantId);
                    Optional<Order> orderOpt = orderService.findById(orderId);
                    if (orderOpt.isPresent()) {
                        Order order = orderOpt.get();
                        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                            amount = order.total();
                        }
                        if (customerName == null || customerName.isBlank()) {
                            customerName = order.customerName();
                        }
                    }
                } finally {
                    TenantContext.clear();
                }
            }

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                amount = BigDecimal.valueOf(10.00); // Default fallback if amount not calculated yet
            }

            String host = httpRequest.getHeader("Host");
            if (host == null || host.isBlank()) {
                host = "localhost:8080";
            }
            String baseUrl = protocol + "://" + host;

            String successUrl = baseUrl + "/" + tenantId + "/order-confirmation?session_id={CHECKOUT_SESSION_ID}";
            String cancelUrl = baseUrl + "/" + tenantId + "/select-payment";

            Session session = stripeService.createRestaurantCheckoutSession(
                    tenantId,
                    orderId != null ? orderId : "ORD-" + System.currentTimeMillis(),
                    amount,
                    "eur",
                    customerName,
                    stripeAccountId,
                    successUrl,
                    cancelUrl
            );

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "checkoutUrl", session.getUrl(),
                    "sessionId", session.getId()
            ));

        } catch (Exception e) {
            log.error("Failed to create restaurant Stripe checkout session", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Level 1: Create Stripe Checkout Session for Tenant Owner subscribing/adding a location.
     */
    @PostMapping("/owner/create-location-checkout-session")
    @ResponseBody
    public ResponseEntity<?> createPlatformCheckoutSession(
            @RequestParam String tenantId,
            @RequestParam(defaultValue = "Pro Plan") String plan,
            Principal principal,
            HttpServletRequest httpRequest) {

        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String ownerUsername = principal.getName();
        log.info("Creating Platform Stripe Checkout Session for owner: {} and tenant location: {}", ownerUsername, tenantId);

        try {
            String host = httpRequest.getHeader("Host");
            if (host == null || host.isBlank()) {
                host = "localhost:8080";
            }
            String baseUrl = protocol + "://" + host;

            String successUrl = baseUrl + "/owner/console?success=true&tenantId=" + tenantId;
            String cancelUrl = baseUrl + "/owner/console?canceled=true";

            Session session = stripeService.createPlatformCheckoutSession(
                    tenantId,
                    ownerUsername,
                    plan,
                    successUrl,
                    cancelUrl
            );

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "checkoutUrl", session.getUrl(),
                    "sessionId", session.getId()
            ));

        } catch (Exception e) {
            log.error("Failed to create platform Stripe checkout session", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
