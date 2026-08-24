package es.brasatech.fastbite.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StripeService {

    private final String secretKey;
    private final String webhookSecret;

    public StripeService(
            @Value("${stripe.secret.key:${STRIPE_SECRET_KEY_SANDBOX:}}") String secretKey,
            @Value("${stripe.webhook.secret:${STRIPE_CONNECT_WEBHOOK_SECRET_SANDBOX:}}") String webhookSecret) {
        this.secretKey = (secretKey != null && !secretKey.isBlank()) 
                ? secretKey 
                : System.getenv("STRIPE_SECRET_KEY_SANDBOX");
        this.webhookSecret = (webhookSecret != null && !webhookSecret.isBlank()) 
                ? webhookSecret 
                : System.getenv("STRIPE_CONNECT_WEBHOOK_SECRET_SANDBOX");

        if (this.secretKey != null && !this.secretKey.isBlank()) {
            Stripe.apiKey = this.secretKey;
        }
    }

    public String getSecretKey() {
        if (secretKey != null && !secretKey.isBlank()) {
            return secretKey;
        }
        String env = System.getenv("STRIPE_SECRET_KEY_SANDBOX");
        if (env != null && !env.isBlank()) {
            return env;
        }
        return getWinEnv("STRIPE_SECRET_KEY_SANDBOX");
    }

    public String getWebhookSecret() {
        if (webhookSecret != null && !webhookSecret.isBlank()) {
            return webhookSecret;
        }
        String env = System.getenv("STRIPE_CONNECT_WEBHOOK_SECRET_SANDBOX");
        if (env != null && !env.isBlank()) {
            return env;
        }
        return getWinEnv("STRIPE_CONNECT_WEBHOOK_SECRET_SANDBOX");
    }

    private String getWinEnv(String name) {
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                Process process = new ProcessBuilder("powershell", "-Command",
                        "[System.Environment]::GetEnvironmentVariable('" + name + "', 'User') + [System.Environment]::GetEnvironmentVariable('" + name + "', 'Machine')").start();
                String val = new String(process.getInputStream().readAllBytes()).trim();
                return val.isBlank() ? null : val;
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Level 1: Platform Level Checkout Session for Tenant Users / Restaurant Owners to purchase subscriptions or additional locations.
     */
    public Session createPlatformCheckoutSession(String tenantId, String ownerUsername, String plan, String successUrl, String cancelUrl) throws StripeException {
        String activeSecretKey = getSecretKey();
        if (activeSecretKey == null || activeSecretKey.isBlank()) {
            throw new IllegalStateException("STRIPE_SECRET_KEY_SANDBOX is not configured.");
        }
        Stripe.apiKey = activeSecretKey;

        Map<String, String> metadata = new HashMap<>();
        metadata.put("tenantId", tenantId);
        metadata.put("ownerUsername", ownerUsername != null ? ownerUsername : "");
        metadata.put("plan", plan != null ? plan : "Pro Plan");
        metadata.put("type", "PLATFORM_SUBSCRIPTION");

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(tenantId)
                .putAllMetadata(metadata)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(2900L) // €29.00 default subscription cost
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("FastBite Platform Location Subscription - " + tenantId)
                                                                .setDescription("Plan: " + (plan != null ? plan : "Standard Plan"))
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        return Session.create(params);
    }

    /**
     * Level 2: Restaurant Level Checkout Session for Guest Customers ordering food at a specific restaurant location.
     */
    public Session createRestaurantCheckoutSession(
            String tenantId,
            String orderId,
            BigDecimal amount,
            String currency,
            String customerName,
            String stripeAccountId,
            String successUrl,
            String cancelUrl) throws StripeException {

        String activeSecretKey = getSecretKey();
        if (activeSecretKey == null || activeSecretKey.isBlank()) {
            throw new IllegalStateException("STRIPE_SECRET_KEY_SANDBOX is not configured.");
        }
        Stripe.apiKey = activeSecretKey;

        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
        if (amountInCents <= 0) {
            amountInCents = 100; // minimum amount
        }

        String curr = (currency != null && !currency.isBlank()) ? currency.toLowerCase() : "eur";

        Map<String, String> metadata = new HashMap<>();
        metadata.put("tenantId", tenantId);
        metadata.put("orderId", orderId);
        metadata.put("customerName", customerName != null ? customerName : "Guest");
        metadata.put("type", "RESTAURANT_ORDER");

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(orderId)
                .putAllMetadata(metadata)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(curr)
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Restaurant Order #" + orderId + " (" + tenantId + ")")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                );

        // If location has a Connected Stripe Account, use destination charge for Connect integration
        if (stripeAccountId != null && !stripeAccountId.isBlank()) {
            builder.setPaymentIntentData(
                    SessionCreateParams.PaymentIntentData.builder()
                            .setTransferData(
                                    SessionCreateParams.PaymentIntentData.TransferData.builder()
                                            .setDestination(stripeAccountId.trim())
                                            .build()
                            )
                            .build()
            );
        }

        SessionCreateParams params = builder.build();

        // Option to execute on behalf of connected account if direct charge
        if (stripeAccountId != null && !stripeAccountId.isBlank()) {
            RequestOptions options = RequestOptions.builder()
                    .setStripeAccount(stripeAccountId.trim())
                    .build();
            try {
                return Session.create(params, options);
            } catch (Exception e) {
                log.warn("Direct charge via StripeAccount header failed, falling back to standard create: {}", e.getMessage());
            }
        }

        return Session.create(params);
    }

    /**
     * Construct and verify Stripe Webhook event signature.
     */
    public Event constructEvent(String payload, String sigHeader) throws SignatureVerificationException {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("STRIPE_CONNECT_WEBHOOK_SECRET_SANDBOX is not set, parsing event unverified");
            return Event.GSON.fromJson(payload, Event.class);
        }
        if (sigHeader == null || sigHeader.isBlank()) {
            log.warn("No Stripe-Signature header provided, parsing event payload directly");
            return Event.GSON.fromJson(payload, Event.class);
        }
        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }
}
