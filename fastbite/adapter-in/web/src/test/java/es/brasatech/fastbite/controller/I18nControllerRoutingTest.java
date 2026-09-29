package es.brasatech.fastbite.controller;

import es.brasatech.fastbite.TestConfig;
import es.brasatech.fastbite.application.discount.DiscountService;
import es.brasatech.fastbite.application.office.CustomizationService;
import es.brasatech.fastbite.application.office.GroupService;
import es.brasatech.fastbite.application.office.I18nConfig;
import es.brasatech.fastbite.application.office.ProductService;
import es.brasatech.fastbite.application.table.TableService;
import es.brasatech.fastbite.application.tenant.TenantLocationService;
import es.brasatech.fastbite.config.TenantRoutingResolver;
import es.brasatech.fastbite.config.WebConfig;
import es.brasatech.fastbite.domain.I18nField;
import es.brasatech.fastbite.domain.product.ProductI18n;
import es.brasatech.fastbite.security.SecurityConfig;
import es.brasatech.fastbite.security.TenantUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = I18nController.class)
@ContextConfiguration(classes = {TestConfig.class, I18nController.class, SecurityConfig.class,
        TenantRoutingResolver.class, WebConfig.class})
class I18nControllerRoutingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupService groupService;
    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private CustomizationService customizationService;
    @MockitoBean
    private TableService tableService;
    @MockitoBean
    private DiscountService discountService;
    @MockitoBean
    private I18nConfig i18nConfig;
    @MockitoBean
    private TenantLocationService tenantLocationService;

    private final ProductI18n kebab = new ProductI18n("p1", new I18nField(Map.of("en", "Kebab", "es", "Kebab ES")),
            BigDecimal.TEN, new I18nField(Map.of("en", "Beef")), "/k.webp", Set.of(), true);

    @BeforeEach
    void setUp() {
        when(i18nConfig.getDefaultLanguage()).thenReturn("en");
        when(i18nConfig.getSupportedLocales()).thenReturn(List.of("en", "es"));
        when(tenantLocationService.getLocationByCustomDomain(anyString())).thenReturn(Optional.empty());
        when(productService.findI18nById("p1")).thenReturn(Optional.of(kebab));
    }

    private static MockHttpSession kebabAdmin() {
        var authorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN");
        var user = new TenantUser("admin", "hash", true, authorities, "kebab");
        var session = new MockHttpSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT",
                new SecurityContextImpl(UsernamePasswordAuthenticationToken.authenticated(user, null, authorities)));
        return session;
    }

    @Test
    void opensTheEditorUnderTheTenantPrefix() throws Exception {
        mockMvc.perform(get("/kebab/backoffice/translations/products/p1").session(kebabAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Kebab ES")));
    }

    @Test
    void savesUnderTheTenantPrefixAndReturnsToThatBackOffice() throws Exception {
        mockMvc.perform(post("/kebab/backoffice/translations/products/p1").session(kebabAdmin()).with(csrf())
                        .param("name_es", "Kebab de ternera"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/kebab/backoffice"));

        ArgumentCaptor<ProductI18n> saved = ArgumentCaptor.forClass(ProductI18n.class);
        verify(productService).updateI18n(eq("p1"), saved.capture());
        assertThat(saved.getValue().name().getAll()).containsEntry("es", "Kebab de ternera").containsEntry("en", "Kebab");
    }

    @Test
    void onTheTenantHostItStaysOnThatHost() throws Exception {
        mockMvc.perform(post("/backoffice/translations/products/p1").header("Host", "kebab.localhost:8080")
                        .session(kebabAdmin()).with(csrf()).param("name_es", "Kebab de ternera"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/backoffice"));
    }
}
