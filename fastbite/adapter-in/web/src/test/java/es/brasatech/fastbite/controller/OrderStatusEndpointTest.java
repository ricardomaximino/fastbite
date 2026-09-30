package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.TestConfig;
import es.brasatech.fastbite.application.kds.KdsConfigService;
import es.brasatech.fastbite.application.order.OrderNumberService;
import es.brasatech.fastbite.application.order.OrderService;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.application.tenant.TenantLocationService;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.domain.order.OrderChannel;
import es.brasatech.fastbite.domain.order.OrderPaymentStatus;
import es.brasatech.fastbite.domain.order.OrderStatus;
import es.brasatech.fastbite.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@ContextConfiguration(classes = {TestConfig.class, OrderController.class, SecurityConfig.class})
class OrderStatusEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private OrderNumberService orderNumberService;
    @MockitoBean
    private TableService tableService;
    @MockitoBean
    private KdsConfigService kdsConfigService;
    @MockitoBean
    private TenantLocationService tenantLocationService;

    @Test
    void guestSeesTheStatusOfTheOrderInTheirSession() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        when(orderService.findById("order-7")).thenReturn(Optional.of(new Order(List.of(), 7, "order-7", now, now,
                OrderStatus.PROCESSING, BigDecimal.TEN, null, OrderPaymentStatus.UNPAID, OrderChannel.TABLE, "es", null, "Ana")));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("orderId", "order-7");

        mockMvc.perform(get("/kebab/api/order-status").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void guestWithoutAnOrderGetsNotFound() throws Exception {
        mockMvc.perform(get("/api/order-status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void guestsCannotListTheKitchenOrders() throws Exception {
        mockMvc.perform(post("/api/order").with(csrf()))
                .andExpect(status().isFound()); // sent to login
    }
}
