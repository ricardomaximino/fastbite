package es.brasatech.fastbite.security;

import es.brasatech.fastbite.TestConfig;
import es.brasatech.fastbite.application.tenant.TenantLocationService;
import es.brasatech.fastbite.config.TenantRoutingResolver;
import es.brasatech.fastbite.controller.MenuController;
import es.brasatech.fastbite.dto.menu.MenuData;
import es.brasatech.fastbite.dto.office.MenuDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MenuController.class)
@DisplayName("SSO Subdomain Integration Tests")
@ContextConfiguration(classes = {
        TestConfig.class, 
        MenuController.class, 
        SecurityConfig.class, 
        es.brasatech.fastbite.config.WebConfig.class
})
class SsoSubdomainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuDataService menuDataService;

    @MockitoBean
    private es.brasatech.fastbite.application.table.TableService tableService;

    @MockitoBean
    private es.brasatech.fastbite.application.table.TableSignatureUtil tableSignatureUtil;

    @MockitoBean
    private es.brasatech.fastbite.application.discount.DiscountService discountService;

    @MockitoBean
    private TenantLocationService tenantLocationService;

    @MockitoBean
    private TenantRoutingResolver tenantRoutingResolver;

    @MockitoBean
    private es.brasatech.fastbite.application.office.UserService userService;

    @BeforeEach
    void setUp() {
        HashMap<String, String> dictionary = new HashMap<>();
        dictionary.put("cartEmpty", "Cart empty");
        dictionary.put("confirmAndSubmitBtn", "Confirm and Submit Order");
        dictionary.put("total", "Total");
        dictionary.put("subtotal", "Subtotal");
        dictionary.put("tax", "Tax");
        dictionary.put("orderConfirmation", "Order Confirmation");
        dictionary.put("orderTotal", "Order Total");
        dictionary.put("orderItems", "Order Items");

        when(menuDataService.buildMenuData(any(Locale.class)))
                .thenReturn(new MenuData(new HashMap<>(), List.of(), "", dictionary, List.of(), ""));

        // Setup owner check mock
        when(tenantLocationService.isOwnerOf("kebabowner", "kebab")).thenReturn(true);
        when(tenantRoutingResolver.resolveTenantId(any())).thenAnswer(invocation -> {
            String host = invocation.getArgument(0);
            if (host != null && host.startsWith("kebab.")) {
                return "kebab";
            }
            return null;
        });
    }


    @Test
    @DisplayName("Accessing kebab.localhost/menu as logged in kebabowner should trigger SSO and display all staff tabs")
    void testSsoSubdomainMenuAccessAsOwner() throws Exception {
        // Create an authenticated session for kebabowner with ROLE_OWNER
        MockHttpSession session = new MockHttpSession();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_OWNER"));
        var auth = new UsernamePasswordAuthenticationToken("kebabowner", "password", authorities);
        var securityContext = new SecurityContextImpl(auth);
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

        mockMvc.perform(get("/menu")
                .header("Host", "kebab.localhost:8080")
                .session(session)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("kebabowner")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("/kebab/dashboard")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("/kebab/counter")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("/kebab/backoffice")));
    }
}

