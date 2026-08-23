package es.brasatech.fastbite.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import static org.junit.jupiter.api.Assertions.*;

public class CookieSerializerTest {

    @Test
    public void testCookieSerializerDomainOnLocalhost() {
        SecurityConfig config = new SecurityConfig();
        CookieSerializer serializer = config.cookieSerializer();
        assertTrue(serializer instanceof DefaultCookieSerializer);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieSerializer.CookieValue cookieValue = new CookieSerializer.CookieValue(request, response, "SESSION-123");
        serializer.writeCookieValue(cookieValue);

        String setCookieHeader = response.getHeader("Set-Cookie");
        assertNotNull(setCookieHeader);
        // On localhost, we must NOT set any Domain attribute (to avoid Chrome rejection and allow host-only login)
        assertFalse(setCookieHeader.contains("Domain="), "Set-Cookie should not contain Domain attribute on localhost");
    }

    @Test
    public void testCookieSerializerDomainOnSubdomainLocalhost() {
        SecurityConfig config = new SecurityConfig();
        CookieSerializer serializer = config.cookieSerializer();
        assertTrue(serializer instanceof DefaultCookieSerializer);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("kebab.localhost");
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieSerializer.CookieValue cookieValue = new CookieSerializer.CookieValue(request, response, "SESSION-123");
        serializer.writeCookieValue(cookieValue);

        String setCookieHeader = response.getHeader("Set-Cookie");
        assertNotNull(setCookieHeader);
        // On kebab.localhost, it should set Domain=kebab.localhost
        assertTrue(setCookieHeader.contains("Domain=kebab.localhost"), "Set-Cookie should contain Domain=kebab.localhost");
    }

    @Test
    public void testCookieSerializerDomainOnProductionSubdomain() {
        SecurityConfig config = new SecurityConfig();
        CookieSerializer serializer = config.cookieSerializer();
        assertTrue(serializer instanceof DefaultCookieSerializer);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("kebab.fastbite.com");
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieSerializer.CookieValue cookieValue = new CookieSerializer.CookieValue(request, response, "SESSION-123");
        serializer.writeCookieValue(cookieValue);

        String setCookieHeader = response.getHeader("Set-Cookie");
        assertNotNull(setCookieHeader);
        // On production kebab.fastbite.com, it should set Domain=fastbite.com to allow wildcard subdomain sharing
        assertTrue(setCookieHeader.contains("Domain=fastbite.com"), "Set-Cookie should contain Domain=fastbite.com");
    }
}
