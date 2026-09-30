package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.TestConfig;
import es.brasatech.fastbite.application.office.CustomizationService;
import es.brasatech.fastbite.application.office.ProductService;
import es.brasatech.fastbite.application.order.OrderNumberService;
import es.brasatech.fastbite.application.order.OrderPricingService;
import es.brasatech.fastbite.application.order.OrderService;
import es.brasatech.fastbite.application.settings.RestaurantSettingsService;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.application.tenant.TenantLocationService;
import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.domain.order.Order;
import es.brasatech.fastbite.domain.order.OrderChannel;
import es.brasatech.fastbite.domain.order.OrderPaymentStatus;
import es.brasatech.fastbite.domain.product.ProductDto;
import es.brasatech.fastbite.domain.settings.RestaurantSettings;
import es.brasatech.fastbite.domain.table.Table;
import es.brasatech.fastbite.domain.table.TableStatus;
import es.brasatech.fastbite.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@ContextConfiguration(classes = {TestConfig.class, OrderController.class, OrderPricingService.class, SecurityConfig.class})
class GuestOrderEndpointTest {

    /** What a guest's browser could send: its own name and price for the product. */
    private static String tamperedCart(String table, int quantity) {
        return """
                {"items":[{"id":"l1","itemId":"kebab","name":"<img src=x onerror=alert(1)>","quantity":%d,"price":0.01,"customizations":[]}],
                 "customerName":"Ana","tableNumber":"%s"}""".formatted(quantity, table);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private OrderNumberService orderNumberService;
    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private CustomizationService customizationService;
    @MockitoBean
    private TableService tableService;
    @MockitoBean
    private RestaurantSettingsService settingsService;
    @MockitoBean
    private TenantLocationService tenantLocationService;

    @BeforeEach
    void setUp() {
        when(settingsService.get()).thenReturn(RestaurantSettings.DEFAULTS);
        when(productService.findById("kebab")).thenReturn(Optional.of(new ProductDto("kebab", "Kebab",
                new BigDecimal("6.50"), "Beef kebab", "/kebab.webp", Set.of(), true)));
        when(tableService.findTableByNameOrId("Table 1")).thenReturn(Optional.of(table("t1", "Table 1")));
        when(tableService.findTableByNameOrId("t2")).thenReturn(Optional.of(table("t2", "Table 2")));
        when(tableService.findTableByNameOrId("Table 9")).thenReturn(Optional.empty());
        when(orderNumberService.next()).thenReturn(12);
        when(orderService.createOrderForTable(any(), anyInt(), any(), any(), any())).thenAnswer(call ->
                new Order(call.getArgument(0), call.getArgument(1), OrderPaymentStatus.UNPAID, OrderChannel.TABLE, "en"));
    }

    private static Table table(String id, String name) {
        return new Table(id, name, 4, TableStatus.AVAILABLE, true, List.of());
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> savedItems(String tableId) {
        ArgumentCaptor<List<CartItem>> items = ArgumentCaptor.forClass(List.class);
        verify(orderService).createOrderForTable(items.capture(), eq(12), eq(tableId), any(), eq("Ana"));
        return items.getValue();
    }

    @Test
    void theOrderIsSavedWithCatalogPricesAndNames() throws Exception {
        mockMvc.perform(post("/kebab/api/create-order").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(tamperedCart("Table 1", 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        assertThat(savedItems("t1")).singleElement().satisfies(item -> {
            assertThat(item.name()).isEqualTo("Kebab");
            assertThat(item.price()).isEqualByComparingTo("6.50");
            assertThat(item.totalPrice()).isEqualByComparingTo("13.00");
        });
    }

    @Test
    void theTableFromTheGuestsQrCodeWinsOverTheForm() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tableNumber", "t2");

        mockMvc.perform(post("/kebab/api/create-order").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(tamperedCart("Table 1", 2)))
                .andExpect(jsonPath("$.status").value("success"));

        savedItems("t2");
    }

    @Test
    void guestsCannotOrderToATableWhenTableServiceIsOff() throws Exception {
        when(settingsService.get()).thenReturn(new RestaurantSettings(false, true, 0, 0));

        mockMvc.perform(post("/kebab/api/create-order").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(tamperedCart("Table 1", 2)))
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("This restaurant is not taking table orders"));

        verify(orderService, never()).createOrderForTable(any(), anyInt(), any(), any(), any());
    }

    @Test
    void aRejectedOrderTakesNoOrderNumber() throws Exception {
        mockMvc.perform(post("/kebab/api/create-order").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(tamperedCart("Table 9", 2)))
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Table does not exist"));
        mockMvc.perform(post("/kebab/api/create-order").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[],\"customerName\":\"Ana\",\"tableNumber\":\"Table 1\"}"))
                .andExpect(jsonPath("$.status").value("error"));
        mockMvc.perform(post("/kebab/api/create-order").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(tamperedCart("Table 1", -2)))
                .andExpect(jsonPath("$.status").value("error"));

        verify(orderNumberService, never()).next();
        verify(orderService, never()).createOrderForTable(any(), anyInt(), any(), any(), any());
    }
}
