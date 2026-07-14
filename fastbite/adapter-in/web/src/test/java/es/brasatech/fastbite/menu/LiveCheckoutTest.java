package es.brasatech.fastbite.menu;

import org.junit.jupiter.api.Test;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@org.junit.jupiter.api.Disabled("Requires a live running server on port 8080")
public class LiveCheckoutTest {

    @Test
    public void testCheckoutFlowOnLiveServer() throws Exception {
        CookieManager cookieManager = new CookieManager();
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        // 1. GET /kebab/menu to retrieve page and CSRF Token
        HttpRequest menuRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/kebab/menu"))
                .GET()
                .build();

        HttpResponse<String> menuResponse = client.send(menuRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, menuResponse.statusCode(), "Failed to load menu page");

        String html = menuResponse.body();
        
        // Extract CSRF Token
        Pattern csrfPattern = Pattern.compile("<meta\\s+name=\"_csrf\"\\s+content=\"([^\"]+)\"");
        Matcher csrfMatcher = csrfPattern.matcher(html);
        assertTrue(csrfMatcher.find(), "Could not find CSRF token in page HTML");
        String csrfToken = csrfMatcher.group(1);
        assertNotNull(csrfToken);
        assertFalse(csrfToken.trim().isEmpty());
        System.out.println("Successfully extracted CSRF token: " + csrfToken);

        // 2. POST /api/create-order
        String jsonPayload = """
        {
          "items": [
            {
              "id": "test-item-id",
              "itemId": "test-product-id",
              "name": "Classic Burger",
              "description": "Juicy beef patty with cheese and lettuce",
              "image": "burger.jpg",
              "quantity": 1,
              "customizations": [],
              "price": 9.99
            }
          ],
          "customerName": "QA Automator",
          "tableNumber": "Table 1",
          "paymentMethod": "cash"
        }
        """;

        HttpRequest createOrderRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/create-order"))
                .header("Content-Type", "application/json")
                .header("X-CSRF-TOKEN", csrfToken)
                .header("Referer", "http://localhost:8080/kebab/menu")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> createOrderResponse = client.send(createOrderRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, createOrderResponse.statusCode(), "Failed to create order");
        String createOrderResponseBody = createOrderResponse.body();
        assertTrue(createOrderResponseBody.contains("success"), "Order creation failed: " + createOrderResponseBody);
        System.out.println("Order created successfully! Response: " + createOrderResponseBody);

        // 3. GET /kebab/select-payment to verify session state
        HttpRequest selectPaymentRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/kebab/select-payment"))
                .GET()
                .build();

        HttpResponse<String> selectPaymentResponse = client.send(selectPaymentRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, selectPaymentResponse.statusCode(), "Failed to load select-payment page");
        assertTrue(selectPaymentResponse.body().contains("Payment Method") || selectPaymentResponse.body().contains("QA Automator") || selectPaymentResponse.body().contains("Classic Burger"), 
                   "select-payment page content mismatch");
        System.out.println("select-payment page loaded successfully!");

        // 4. GET /kebab/order-confirmation
        HttpRequest confirmationRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/kebab/order-confirmation"))
                .GET()
                .build();

        HttpResponse<String> confirmationResponse = client.send(confirmationRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, confirmationResponse.statusCode(), "Failed to load order-confirmation page");
        String confBody = confirmationResponse.body();
        System.out.println("Confirmation body length: " + confBody.length());
        if (!confBody.contains("Confirmation") && !confBody.contains("Order Received") && !confBody.contains("ORD-") && !confBody.contains("Order Submitted") && !confBody.contains("número") && !confBody.contains("pedido")) {
            System.out.println("CONFIRMATION HTML BODY:\n" + confBody);
            fail("order-confirmation page content mismatch");
        }
        System.out.println("order-confirmation page loaded successfully!");
    }
}
