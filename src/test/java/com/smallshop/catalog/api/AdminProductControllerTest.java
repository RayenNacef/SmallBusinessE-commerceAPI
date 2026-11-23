package com.smallshop.catalog.api;

import com.smallshop.catalog.application.ChangeProductAvailability;
import com.smallshop.catalog.application.CreateProduct;
import com.smallshop.catalog.application.ManageProductListing;
import com.smallshop.catalog.application.UpdateProduct;
import com.smallshop.catalog.domain.CategoryId;
import com.smallshop.catalog.domain.CategoryNotFoundException;
import com.smallshop.catalog.domain.Product;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.user.infrastructure.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.smallshop.TestTokens.asAdmin;
import static com.smallshop.TestTokens.asCustomer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminProductController.class)
@Import(SecurityConfiguration.class)
@MockitoBean(types = {JwtDecoder.class, UpdateProduct.class, ManageProductListing.class})
class AdminProductControllerTest {

    private static final String VALID_PRODUCT = """
            {"name": "Espresso beans", "description": "Dark roast", "price": 12.50,
             "categoryId": "%s", "initialStock": 10}
            """.formatted(UUID.randomUUID());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateProduct createProduct;

    @MockitoBean
    private ChangeProductAvailability changeAvailability;

    @Test
    void anonymousUserCannotCreateProducts() throws Exception {
        mockMvc.perform(post("/api/admin/products").contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotCreateProducts() throws Exception {
        mockMvc.perform(post("/api/admin/products").with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreatesAProduct() throws Exception {
        Product product = Product.create("Espresso beans", "Dark roast", Money.of("12.50"), CategoryId.newId());
        when(createProduct.create(any(), anyInt())).thenReturn(product);

        mockMvc.perform(post("/api/admin/products").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/products/" + product.id()))
                .andExpect(jsonPath("$.id").value(product.id().toString()))
                .andExpect(jsonPath("$.price").value(12.50))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void invalidProductIsRejectedWithFieldErrors() throws Exception {
        String invalid = """
                {"name": "", "price": -1, "categoryId": null, "initialStock": -5}
                """;

        mockMvc.perform(post("/api/admin/products").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.categoryId").exists())
                .andExpect(jsonPath("$.errors.initialStock").exists());
    }

    @Test
    void priceWithMoreThanTwoDecimalsIsRejected() throws Exception {
        String invalid = VALID_PRODUCT.replace("12.50", "12.505");

        mockMvc.perform(post("/api/admin/products").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void unknownCategoryGivesNotFound() throws Exception {
        when(createProduct.create(any(), anyInt())).thenThrow(new CategoryNotFoundException(CategoryId.newId()));

        mockMvc.perform(post("/api/admin/products").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingAProductDeactivatesIt() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/products/{id}", id).with(asAdmin()))
                .andExpect(status().isNoContent());

        verify(changeAvailability).deactivate(new ProductId(id));
    }
}
