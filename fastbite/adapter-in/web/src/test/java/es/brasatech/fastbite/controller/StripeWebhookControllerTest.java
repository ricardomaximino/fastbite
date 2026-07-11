package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.TestConfig;
import es.brasatech.fastbite.application.tenant.TenantSignupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
    private TenantSignupService tenantSignupService;

    @MockitoBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /api/webhooks/stripe - Should parse event and invoke signup service")
    void testHandleStripeWebhook() throws Exception {
        org.mockito.Mockito.when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");
        String payload = """
            {
              "id": "evt_test_123",
              "object": "event",
              "type": "checkout.session.completed",
              "data": {
                "object": {
                  "id": "cs_test_999",
                  "client_reference_id": "stripeburger",
                  "customer_details": {
                    "email": "burger@stripe.com",
                    "name": "Stripe Burger Corp"
                  },
                  "metadata": {
                    "tenantId": "stripeburger",
                    "username": "stripeadmin",
                    "fullName": "Stripe Owner"
                  }
                }
              }
            }
            """;

        mockMvc.perform(post("/api/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .with(csrf())) // csrf token bypass is handled by SecurityConfig, but mockMvc security support requires this or csrf()
                .andDo(print())
                .andExpect(status().isOk());

        verify(tenantSignupService).registerTenant(
                eq("stripeburger"),
                eq("stripeadmin"),
                anyString(),
                eq("Stripe Owner")
        );
    }
}
