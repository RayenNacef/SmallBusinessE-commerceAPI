package com.smallshop.catalog.api;

import com.smallshop.catalog.application.BrowseProducts;
import com.smallshop.catalog.domain.CategoryId;
import com.smallshop.catalog.domain.Product;
import com.smallshop.catalog.domain.ProductNotFoundException;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.user.infrastructure.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfiguration.class)
@MockitoBean(types = JwtDecoder.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrowseProducts browseProducts;

    @Test
    void anyoneCanBrowseProductsWithoutLoggingIn() throws Exception {
        CategoryId coffee = CategoryId.newId();
        Product product = Product.create("Espresso beans", "", Money.of("12.50"), coffee);
        when(browseProducts.browse(any(), any(), any())).thenReturn(new PageResult<>(List.of(product), 1, 5, 6, 2));

        mockMvc.perform(get("/api/products")
                        .param("page", "1").param("size", "5")
                        .param("categoryId", coffee.toString()).param("search", "espresso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Espresso beans"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalItems").value(6))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(browseProducts).browse(Optional.of(coffee), Optional.of("espresso"), new PageQuery(1, 5));
    }

    @Test
    void pageSizeAboveTheLimitIsRejected() throws Exception {
        mockMvc.perform(get("/api/products").param("size", "1000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedCategoryIdIsRejected() throws Exception {
        mockMvc.perform(get("/api/products").param("categoryId", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inactiveOrUnknownProductIsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(browseProducts.getActive(new ProductId(id))).thenThrow(new ProductNotFoundException(new ProductId(id)));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }
}
