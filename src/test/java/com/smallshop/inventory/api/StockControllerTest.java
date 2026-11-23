package com.smallshop.inventory.api;

import com.smallshop.inventory.application.ManageStockLevel;
import com.smallshop.inventory.domain.StockItem;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
@Import(SecurityConfiguration.class)
@MockitoBean(types = JwtDecoder.class)
class StockControllerTest {

    private final UUID productId = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageStockLevel manageStockLevel;

    @Test
    void adminSetsTheStockLevel() throws Exception {
        when(manageStockLevel.set(new ProductId(productId), 25)).thenReturn(new StockItem(new ProductId(productId), 25));

        mockMvc.perform(put("/api/admin/products/{id}/stock", productId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"available\": 25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(25));
    }

    @Test
    void negativeStockIsRejected() throws Exception {
        mockMvc.perform(put("/api/admin/products/{id}/stock", productId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"available\": -1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void customerCannotChangeStock() throws Exception {
        mockMvc.perform(put("/api/admin/products/{id}/stock", productId).with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"available\": 25}"))
                .andExpect(status().isForbidden());
    }
}
