package es.brasatech.fastbite.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import es.brasatech.fastbite.application.tenant.TenantSignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final TenantSignupService tenantSignupService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @PostMapping("/api/webhooks/stripe")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload) {
        log.info("Received Stripe webhook notification");
        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.path("type").asText();
            log.info("Processing Stripe event type: {}", eventType);

            if ("checkout.session.completed".equals(eventType) || "customer.subscription.created".equals(eventType)) {
                JsonNode dataObject = root.path("data").path("object");
                
                // Try to get tenantId from metadata, fallback to client_reference_id
                String tenantId = dataObject.path("metadata").path("tenantId").asText();
                if (tenantId.isEmpty()) {
                    tenantId = dataObject.path("client_reference_id").asText();
                }

                if (tenantId.isEmpty()) {
                    log.warn("Stripe webhook event does not contain tenantId / client_reference_id!");
                    return ResponseEntity.badRequest().body("Missing tenant identifier");
                }

                String username = dataObject.path("metadata").path("username").asText("admin");
                String fullName = dataObject.path("metadata").path("fullName").asText();
                if (fullName.isEmpty()) {
                    fullName = dataObject.path("customer_details").path("name").asText("Restaurant Owner");
                }

                // Generate a temporary password for self-provisioned admin
                String temporaryPassword = "Temp" + tenantId + "2026!";
                String encodedPassword = passwordEncoder.encode(temporaryPassword);

                log.info("Auto-provisioning tenant: {} via Stripe billing event", tenantId);
                tenantSignupService.registerTenant(tenantId, username, encodedPassword, fullName);
                log.info("Successfully provisioned tenant: {} with temporary password: {}", tenantId, temporaryPassword);
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Failed to process Stripe webhook payload: ", e);
            return ResponseEntity.status(500).body("Error processing webhook: " + e.getMessage());
        }
    }
}
