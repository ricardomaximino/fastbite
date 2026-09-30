package es.brasatech.fastbite.menu;

import es.brasatech.fastbite.TestConfig;
import es.brasatech.fastbite.application.discount.DiscountService;
import es.brasatech.fastbite.application.office.CustomizationService;
import es.brasatech.fastbite.application.office.ProductService;
import es.brasatech.fastbite.application.order.OrderPricingService;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.application.tenant.TenantLocationService;
import es.brasatech.fastbite.controller.BadRequestAdvice;
import es.brasatech.fastbite.controller.MenuController;
import es.brasatech.fastbite.domain.customization.CustomizationDto;
import es.brasatech.fastbite.domain.customization.CustomizationOptionDto;
import es.brasatech.fastbite.domain.product.ProductDto;
import es.brasatech.fastbite.dto.office.MenuDataService;
import es.brasatech.fastbite.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The guest's cart and confirmation show what the order will really cost, whatever the browser sent. */
@WebMvcTest(controllers = MenuController.class, properties = "fastbite.tax.percentage=10.0")
@ContextConfiguration(classes = {TestConfig.class, MenuController.class, OrderPricingService.class,
        BadRequestAdvice.class, SecurityConfig.class})
class GuestCartPricingTest {

    private static final String TAMPERED_CART = """
            [{"id":"l1","itemId":"kebab","name":"Hacked","description":"x","image":"/x.png","quantity":2,"price":0.01,
              "customizations":[{"id":"toppings-opt-0","name":"Free","price":-9,"quantity":1}]}]""";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private CustomizationService customizationService;
    @MockitoBean
    private DiscountService discountService;
    @MockitoBean
    private MenuDataService menuDataService;
    @MockitoBean
    private TableService tableService;
    @MockitoBean
    private TenantLocationService tenantLocationService;

    @BeforeEach
    void setUp() {
        when(productService.findById("kebab")).thenReturn(Optional.of(new ProductDto("kebab", "Kebab",
                new BigDecimal("6.50"), "Beef kebab", "/kebab.webp", Set.of("toppings"), true)));
        when(productService.findById("gone")).thenReturn(Optional.empty());
        when(customizationService.findById("toppings")).thenReturn(Optional.of(new CustomizationDto("toppings",
                "Toppings", "checkbox", List.of(new CustomizationOptionDto("toppings-opt-0", "Extra cheese",
                new BigDecimal("0.50"), false, 0)), 0)));
        when(discountService.calculateDiscount(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(discountService.calculateCartBreakdown(any(), any(), any(), anyDouble())).thenCallRealMethod();
    }

    @Test
    void theCartIsRenderedWithCatalogNamesAndPrices() throws Exception {
        mockMvc.perform(post("/kebab/api/calculate-cart").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(TAMPERED_CART))
                .andExpect(status().isOk())
                .andExpect(model().attribute("subtotal", new BigDecimal("14.00"))) // (6.50 + 0.50) x 2
                .andExpect(content().string(containsString("Kebab")))
                .andExpect(content().string(containsString("Extra cheese")))
                .andExpect(content().string(not(containsString("Hacked"))));
    }

    @Test
    void theConfirmationIsRenderedWithCatalogPricesToo() throws Exception {
        mockMvc.perform(post("/kebab/api/calculate-confirmation").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(TAMPERED_CART))
                .andExpect(status().isOk())
                .andExpect(model().attribute("total", new BigDecimal("14.00")))
                .andExpect(content().string(not(containsString("Hacked"))));
    }

    @Test
    void aCartWithAProductThatLeftTheMenuIsRejectedWithTheReason() throws Exception {
        mockMvc.perform(post("/kebab/api/calculate-cart").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("[{\"itemId\":\"gone\",\"quantity\":1}]"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("A product in this order is no longer on the menu"));
    }
}
