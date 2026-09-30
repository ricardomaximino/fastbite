package es.brasatech.fastbite.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import es.brasatech.fastbite.application.tenant.TenantLocationService;
import es.brasatech.fastbite.application.tenant.TenantSignupService;
import es.brasatech.fastbite.service.OrderCheckoutService;
import es.brasatech.fastbite.service.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final StripeService stripeService;
    private final TenantSignupService tenantSignupService;
    private final TenantLocationService tenantLocationService;
    private final OrderCheckoutService orderCheckoutService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @PostMapping("/api/webhooks/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        log.info("Received Stripe webhook notification");
        try {
            Event event = stripeService.constructEvent(payload, sigHeader);
            String eventType = event != null && event.getType() != null ? event.getType() : "";

            if (eventType.isEmpty()) {
                JsonNode root = objectMapper.readTree(payload);
                eventType = root.path("type").asText();
            }
            log.info("Processing verified Stripe event type: {}", eventType);

            if ("checkout.session.completed".equals(eventType) || "checkout.session.async_payment_succeeded".equals(eventType)
                    || "customer.subscription.created".equals(eventType)) {
                Session session = null;
                if (event != null) {
                    try {
                        var dataObjectDeserializer = event.getDataObjectDeserializer();
                        if (dataObjectDeserializer != null && dataObjectDeserializer.getObject().isPresent() && dataObjectDeserializer.getObject().get() instanceof Session s) {
                            session = s;
                        } else if (dataObjectDeserializer != null) {
                            if (dataObjectDeserializer.deserializeUnsafe() instanceof Session s) {
                                session = s;
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Stripe event deserialization skipped: {}", e.getMessage());
                    }
                }

                Map<String, String> metadata = new HashMap<>();
                String clientReferenceId = "";
                String checkoutSessionId = session != null ? session.getId() : null;

                if (session != null) {
                    if (session.getMetadata() != null) {
                        metadata.putAll(session.getMetadata());
                    }
                    if (session.getClientReferenceId() != null) {
                        clientReferenceId = session.getClientReferenceId();
                    }
                } else {
                    try {
                        JsonNode root = objectMapper.readTree(payload);
                        JsonNode obj = root.path("data").path("object");
                        clientReferenceId = obj.path("client_reference_id").asText();
                        checkoutSessionId = obj.path("id").asText();
                        JsonNode metaNode = obj.path("metadata");
                        if (metaNode.isObject()) {
                            metaNode.forEachEntry((key, val) -> metadata.put(key, val.asText()));
                        }
                    } catch (Exception ex) {
                        log.warn("Could not parse payload JsonNode fallback: {}", ex.getMessage());
                    }
                }

                String tenantId = metadata.getOrDefault("tenantId", clientReferenceId);
                String orderId = metadata.getOrDefault("orderId", clientReferenceId);
                String paymentType = metadata.getOrDefault("type", "");

                // ===== LEVEL 2: RESTAURANT LEVEL PAYMENT (FOOD ORDER) =====
                if ("RESTAURANT_ORDER".equalsIgnoreCase(paymentType) || (!orderId.isEmpty() && !"PLATFORM_SUBSCRIPTION".equalsIgnoreCase(paymentType))) {
                    if (tenantId.isEmpty()) {
                        log.warn("Restaurant order webhook missing tenantId!");
                        return ResponseEntity.badRequest().body("Missing tenant identifier for order payment");
                    }
                    log.info("Processing Restaurant Order Payment for order: {} in tenant: {}", orderId, tenantId);
                    // What was paid, and for which order, is read back from Stripe rather than from this call
                    if (checkoutSessionId == null || checkoutSessionId.isBlank()) {
                        return ResponseEntity.badRequest().body("Missing checkout session");
                    }
                    orderCheckoutService.confirm(stripeService.retrieveSession(checkoutSessionId));
                } 
                // ===== LEVEL 1: PLATFORM LEVEL PAYMENT (OWNER SUBSCRIPTION / LOCATION PROVISIONING) =====
                else {
                    if (tenantId.isEmpty()) {
                        log.warn("Stripe webhook event does not contain tenantId / client_reference_id!");
                        return ResponseEntity.badRequest().body("Missing tenant identifier");
                    }

                    String ownerUsername = metadata.getOrDefault("ownerUsername", "");
                    String plan = metadata.getOrDefault("plan", "Standard Plan");

                    if (!ownerUsername.isEmpty() && tenantLocationService.getLocation(tenantId).isEmpty()) {
                        log.info("Auto-registering additional location: {} for owner: {}", tenantId, ownerUsername);
                        tenantSignupService.registerAdditionalLocation(tenantId, ownerUsername, plan);
                    } else if (tenantLocationService.getLocation(tenantId).isEmpty()) {
                        String username = metadata.getOrDefault("username", "admin");
                        String fullName = metadata.getOrDefault("fullName", "Restaurant Owner");

                        String temporaryPassword = "Temp" + tenantId + "2026!";
                        String encodedPassword = passwordEncoder.encode(temporaryPassword);

                        log.info("Auto-provisioning tenant: {} via Stripe billing event", tenantId);
                        tenantSignupService.registerTenant(tenantId, username, encodedPassword, fullName);
                        log.info("Successfully provisioned tenant: {} with temp password", tenantId);
                    } else {
                        log.info("Tenant location {} is already provisioned", tenantId);
                    }
                }
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (SignatureVerificationException e) {
            log.warn("Refused Stripe webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            log.error("Failed to process Stripe webhook payload: ", e);
            return ResponseEntity.status(500).body("Error processing webhook: " + e.getMessage());
        }
    }
}
