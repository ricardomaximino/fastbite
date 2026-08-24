package es.brasatech.fastbite.controller;

import com.stripe.model.Event;
import es.brasatech.fastbite.TestConfig;
import es.brasatech.fastbite.application.order.OrderService;
import es.brasatech.fastbite.application.tenant.TenantLocationService;
import es.brasatech.fastbite.application.tenant.TenantSignupService;
import es.brasatech.fastbite.service.StripeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@WebMvcTest(controllers = StripeWebhookController.class)
@DisplayName("StripeWebhookController Tests")
@ContextConfiguration(classes = {TestConfig.class, StripeWebhookController.class, es.brasatech.fastbite.security.SecurityConfig.class})
class StripeWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StripeService stripeService;

    @MockitoBean
    private TenantSignupService tenantSignupService;

    @MockitoBean
    private TenantLocationService tenantLocationService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private tools.jackson.databind.ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/webhooks/stripe - Platform level event should auto-provision tenant")
    void testHandleStripeWebhookPlatformLevel() throws Exception {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");
        String payload = """
            {
              "id": "evt_test_123",
              "object": "event",
              "type": "checkout.session.completed",
              "data": {
                "object": {
                  "id": "cs_test_999",
                  "client_reference_id": "stripeburger",
                  "metadata": {
                    "tenantId": "stripeburger",
                    "username": "stripeadmin",
                    "fullName": "Stripe Owner",
                    "type": "PLATFORM_SUBSCRIPTION"
                  }
                }
              }
            }
            """;

        Event mockEvent = Event.GSON.fromJson(payload, Event.class);
        when(stripeService.constructEvent(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.nullable(String.class))).thenReturn(mockEvent);
        when(tenantLocationService.getLocation("stripeburger")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(tenantSignupService).registerTenant(
                eq("stripeburger"),
                eq("stripeadmin"),
                anyString(),
                eq("Stripe Owner")
        );
    }

    @Test
    @DisplayName("POST /api/webhooks/stripe - Restaurant level event should mark order paid")
    void testHandleStripeWebhookRestaurantLevel() throws Exception {
        String payload = """
            {
              "id": "evt_test_456",
              "object": "event",
              "type": "checkout.session.completed",
              "data": {
                "object": {
                  "id": "cs_test_888",
                  "client_reference_id": "ORD-101",
                  "metadata": {
                    "tenantId": "kebab",
                    "orderId": "ORD-101",
                    "type": "RESTAURANT_ORDER"
                  }
                }
              }
            }
            """;

        Event mockEvent = Event.GSON.fromJson(payload, Event.class);
        when(stripeService.constructEvent(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.nullable(String.class))).thenReturn(mockEvent);

        mockMvc.perform(post("/api/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(orderService).markOrderPaid("ORD-101");
    }
}
