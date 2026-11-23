package com.smallshop;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ShopFlowIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@smallshop.test";
    private static final String ADMIN_PASSWORD = "admin-password";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void customerBuysCancelsAndTheShopKeepsStockAndPricesConsistent() throws Exception {
        String admin = logIn(ADMIN_EMAIL, ADMIN_PASSWORD);

        String categoryId = idOf(perform(post("/api/admin/categories"), admin,
                "{\"name\": \"Coffee %s\"}".formatted(UUID.randomUUID()), 201));
        String productId = idOf(perform(post("/api/admin/products"), admin, """
                {"name": "Espresso beans", "description": "Dark roast", "price": 12.50,
                 "categoryId": "%s", "initialStock": 5}
                """.formatted(categoryId), 201));

        String customerEmail = "buyer-" + UUID.randomUUID() + "@example.com";
        perform(post("/api/auth/register"), null,
                "{\"email\": \"%s\", \"password\": \"secret-password\"}".formatted(customerEmail), 201);
        String customer = logIn(customerEmail, "secret-password");

        perform(post("/api/cart/items"), customer, "{\"productId\": \"%s\", \"quantity\": 2}".formatted(productId), 200);
        MvcResult placed = perform(post("/api/orders"), customer, null, 201);
        String orderId = idOf(placed);
        expectStock(admin, productId, 3);

        perform(get("/api/cart"), customer, null, 200);
        mockMvc.perform(get("/api/cart").header(HttpHeaders.AUTHORIZATION, bearer(customer)))
                .andExpect(jsonPath("$.lines").isEmpty());

        perform(put("/api/admin/products/" + productId), admin, """
                {"name": "Espresso beans", "description": "Dark roast", "price": 99.99, "categoryId": "%s"}
                """.formatted(categoryId), 200);
        mockMvc.perform(get("/api/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, bearer(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].unitPrice").value(12.50))
                .andExpect(jsonPath("$.total").value(25.00));

        perform(post("/api/orders/" + orderId + "/cancel"), customer, null, 200);
        expectStock(admin, productId, 5);
        perform(post("/api/orders/" + orderId + "/cancel"), customer, null, 409);

        perform(delete("/api/admin/products/" + productId), admin, null, 204);
        perform(get("/api/products/" + productId), null, null, 404);
        mockMvc.perform(get("/api/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, bearer(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].productName").value("Espresso beans"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void adminMovesAnOrderThroughItsLifecycleAndCustomerCanNoLongerCancel() throws Exception {
        String admin = logIn(ADMIN_EMAIL, ADMIN_PASSWORD);
        String categoryId = idOf(perform(post("/api/admin/categories"), admin,
                "{\"name\": \"Tea %s\"}".formatted(UUID.randomUUID()), 201));
        String productId = idOf(perform(post("/api/admin/products"), admin, """
                {"name": "Sencha", "price": 8.00, "categoryId": "%s", "initialStock": 1}
                """.formatted(categoryId), 201));
        String customerEmail = "tea-" + UUID.randomUUID() + "@example.com";
        perform(post("/api/auth/register"), null,
                "{\"email\": \"%s\", \"password\": \"secret-password\"}".formatted(customerEmail), 201);
        String customer = logIn(customerEmail, "secret-password");
        perform(post("/api/cart/items"), customer, "{\"productId\": \"%s\", \"quantity\": 1}".formatted(productId), 200);
        String orderId = idOf(perform(post("/api/orders"), customer, null, 201));

        perform(post("/api/admin/orders/" + orderId + "/ship"), admin, null, 409);
        perform(post("/api/admin/orders/" + orderId + "/pay"), admin, null, 200);
        perform(post("/api/admin/orders/" + orderId + "/ship"), admin, null, 200);

        perform(post("/api/orders/" + orderId + "/cancel"), customer, null, 409);
        perform(post("/api/admin/orders/" + orderId + "/deliver"), admin, null, 200);
        expectStock(admin, productId, 0);
    }

    @Test
    void forgedTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/cart").header(HttpHeaders.AUTHORIZATION, "Bearer not.a.real-token"))
                .andExpect(status().isUnauthorized());
    }

    private String logIn(String email, String password) throws Exception {
        MvcResult result = perform(post("/api/auth/login"), null,
                "{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, password), 200);
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private void expectStock(String adminToken, String productId, int expected) throws Exception {
        mockMvc.perform(get("/api/admin/products/" + productId + "/stock").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(expected));
    }

    private MvcResult perform(MockHttpServletRequestBuilder request, String token, String body, int expectedStatus) throws Exception {
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, bearer(token));
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(request).andExpect(status().is(expectedStatus)).andReturn();
    }

    private static String idOf(MvcResult result) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
