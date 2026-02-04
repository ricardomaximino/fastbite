package es.brasatech.fastbite.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.brasatech.fastbite.TestConfig;
import es.brasatech.fastbite.controller.MenuController;
import es.brasatech.fastbite.domain.order.CartItem;
import es.brasatech.fastbite.dto.menu.MenuData;
import es.brasatech.fastbite.dto.menu.OrderDto;
import es.brasatech.fastbite.dto.office.MenuDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(MenuController.class)
@DisplayName("MenuController Tests")
@ContextConfiguration(classes = {TestConfig.class, MenuController.class})
class MenuControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private MenuDataService menuDataService;

        private List<CartItem> testCartItems;
        private MockHttpSession mockSession;

        @BeforeEach
        void setUp() {

                // Mock MenuDataService to return MenuData with required dictionary entries
                Map<String, String> dictionary = new HashMap<>();
                dictionary.put("cartEmpty", "Your cart is empty");
                dictionary.put("confirmAndSubmitBtn", "Confirm and Submit Order");
                dictionary.put("total", "Total");
                dictionary.put("subtotal", "Subtotal");
                dictionary.put("tax", "Tax");
                dictionary.put("orderConfirmation", "Order Confirmation");
                dictionary.put("orderTotal", "Order Total");
                dictionary.put("orderItems", "Order Items");

                when(menuDataService.buildMenuData(any(Locale.class)))
                                .thenReturn(new MenuData(new HashMap<>(), List.of(), "", dictionary, List.of(), ""));

                // Create test cart items
                testCartItems = new ArrayList<>();
                testCartItems.add(new CartItem(
                                "1",
                                "item-1",
                                "Burger",
                                "Delicious burger",
                                "burger.jpg",
                                2,
                                new ArrayList<>(),
                                new BigDecimal("10.00")));
                testCartItems.add(new CartItem(
                                "2",
                                "item-2",
                                "Fries",
                                "Crispy fries",
                                "fries.jpg",
                                1,
                                new ArrayList<>(),
                                new BigDecimal("5.00")));

                // Setup mock session
                mockSession = new MockHttpSession();
                mockSession.setAttribute("orderNumber", "ORD-12345");
                mockSession.setAttribute("cart", testCartItems);
        }

        @Test
        @DisplayName("GET /menu - Should return menu page")
        void testGetMenu() throws Exception {
                mockMvc.perform(get("/menu"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(view().name("fastfood/menu"))
                                .andExpect(model().attributeExists("menuData"));
        }

        @Test
        @DisplayName("POST /api/calculate-cart - Should calculate cart totals and return cart fragment")
        void testCalculateCart() throws Exception {
                String cartJson = objectMapper.writeValueAsString(testCartItems);

                mockMvc.perform(post("/api/calculate-cart")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cartJson))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(view().name("fastfood/fragments/menu :: #cart"))
                                .andExpect(model().attributeExists("cart"))
                                .andExpect(model().attributeExists("subtotal"))
                                .andExpect(model().attributeExists("tax"))
                                .andExpect(model().attributeExists("total"))
                                .andExpect(model().attribute("cart", hasSize(2)))
                                .andExpect(model().attribute("subtotal", new BigDecimal("25.00")))
                                .andExpect(model().attribute("tax", new BigDecimal("2.50")))
                                .andExpect(model().attribute("total", new BigDecimal("25.00")));
        }

        @Test
        @DisplayName("POST /api/calculate-cart - Should handle empty cart")
        void testCalculateCartEmpty() throws Exception {
                String emptyCartJson = objectMapper.writeValueAsString(new ArrayList<>());

                mockMvc.perform(post("/api/calculate-cart")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(emptyCartJson))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(view().name("fastfood/fragments/menu :: #cart"))
                                .andExpect(model().attributeExists("cart"))
                                .andExpect(model().attribute("cart", hasSize(0)))
                                .andExpect(model().attribute("subtotal", new BigDecimal("0")))
                                .andExpect(model().attribute("tax", new BigDecimal("0.00")))
                                .andExpect(model().attribute("total", new BigDecimal("0")));
        }

        @Test
        @DisplayName("POST /api/calculate-confirmation - Should calculate confirmation totals and return confirmation fragment")
        void testCalculateConfirmation() throws Exception {
                String cartJson = objectMapper.writeValueAsString(testCartItems);

                mockMvc.perform(post("/api/calculate-confirmation")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cartJson))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(view().name("fastfood/fragments/menu :: #confirmation"))
                                .andExpect(model().attributeExists("cart"))
                                .andExpect(model().attributeExists("subtotal"))
                                .andExpect(model().attributeExists("tax"))
                                .andExpect(model().attributeExists("total"))
                                .andExpect(model().attribute("cart", hasSize(2)))
                                .andExpect(model().attribute("subtotal", new BigDecimal("25.00")))
                                .andExpect(model().attribute("tax", new BigDecimal("2.50")))
                                .andExpect(model().attribute("total", new BigDecimal("25.00")));
        }

        @Test
        @DisplayName("POST /api/calculate-confirmation - Should handle single item")
        void testCalculateConfirmationSingleItem() throws Exception {
                List<CartItem> singleItem = List.of(new CartItem(
                                "1",
                                "item-1",
                                "Pizza",
                                "Delicious pizza",
                                "pizza.jpg",
                                1,
                                new ArrayList<>(),
                                new BigDecimal("15.00")));
                String cartJson = objectMapper.writeValueAsString(singleItem);

                mockMvc.perform(post("/api/calculate-confirmation")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cartJson))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(view().name("fastfood/fragments/menu :: #confirmation"))
                                .andExpect(model().attribute("cart", hasSize(1)))
                                .andExpect(model().attribute("subtotal", new BigDecimal("15.00")))
                                .andExpect(model().attribute("tax", new BigDecimal("1.50")))
                                .andExpect(model().attribute("total", new BigDecimal("15.00")));
        }

        @Test
        @DisplayName("GET /select-payment - Should return payment selection page with order from session")
        void testSelectPayment() throws Exception {
                mockMvc.perform(get("/select-payment").session(mockSession))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(view().name("fastfood/paymentSelection"))
                                .andExpect(model().attributeExists("order"))
                                .andExpect(result -> {
                                        var order = (OrderDto) result.getModelAndView()
                                                        .getModel().get("order");
                                        assert order != null;
                                        assert "ORD-12345".equals(order.id());
                                        assert order.itemList().size() == 2;
                                });
        }

        @Test
        @DisplayName("GET /select-payment - Should handle missing cart in session")
        void testSelectPaymentWithoutCart() throws Exception {
                MockHttpSession emptySession = new MockHttpSession();
                emptySession.setAttribute("orderNumber", "ORD-67890");

                mockMvc.perform(get("/select-payment").session(emptySession))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(view().name("fastfood/paymentSelection"))
                                .andExpect(model().attributeExists("order"))
                                .andExpect(result -> {
                                        var order = (OrderDto) result.getModelAndView()
                                                        .getModel().get("order");
                                        assert order != null;
                                        assert "ORD-67890".equals(order.id());
                                        assert order.itemList().isEmpty();
                                });
        }

        @Test
        @DisplayName("GET /select-payment - Should handle null session attributes")
        void testSelectPaymentWithNullSession() throws Exception {
                MockHttpSession nullSession = new MockHttpSession();

                mockMvc.perform(get("/select-payment").session(nullSession))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(view().name("fastfood/paymentSelection"))
                                .andExpect(model().attributeExists("order"))
                                .andExpect(result -> {
                                        var order = (OrderDto) result.getModelAndView()
                                                        .getModel().get("order");
                                        assert order != null;
                                        assert order.id() == null;
                                        assert order.itemList().isEmpty();
                                });
        }

        @Test
        @DisplayName("POST /api/calculate-cart - Should calculate tax correctly at 10%")
        void testTaxCalculation() throws Exception {
                List<CartItem> itemsForTaxTest = List.of(new CartItem(
                                "1",
                                "item-1",
                                "Test Item",
                                "Description",
                                "image.jpg",
                                1,
                                new ArrayList<>(),
                                new BigDecimal("100.00")));
                String cartJson = objectMapper.writeValueAsString(itemsForTaxTest);

                mockMvc.perform(post("/api/calculate-cart")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cartJson))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("subtotal", new BigDecimal("100.00")))
                                .andExpect(model().attribute("tax", new BigDecimal("10.00")))
                                .andExpect(model().attribute("total", new BigDecimal("100.00")));
        }

        @Test
        @DisplayName("POST /api/calculate-cart - Should handle multiple quantities correctly")
        void testMultipleQuantities() throws Exception {
                List<CartItem> multiQuantityItems = List.of(new CartItem(
                                "1",
                                "item-1",
                                "Burger",
                                "Description",
                                "burger.jpg",
                                5,
                                new ArrayList<>(),
                                new BigDecimal("10.00")));
                String cartJson = objectMapper.writeValueAsString(multiQuantityItems);

                mockMvc.perform(post("/api/calculate-cart")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cartJson))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("subtotal", new BigDecimal("50.00")))
                                .andExpect(model().attribute("tax", new BigDecimal("5.00")))
                                .andExpect(model().attribute("total", new BigDecimal("50.00")));
        }
}
