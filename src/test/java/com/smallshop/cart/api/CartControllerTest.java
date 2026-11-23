package com.smallshop.cart.api;

import com.smallshop.cart.application.AddToCart;
import com.smallshop.cart.application.CartView;
import com.smallshop.cart.application.ChangeCartItemQuantity;
import com.smallshop.cart.application.RemoveFromCart;
import com.smallshop.cart.application.ViewCart;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import com.smallshop.user.infrastructure.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.smallshop.TestTokens.asAdmin;
import static com.smallshop.TestTokens.asCustomer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(SecurityConfiguration.class)
@MockitoBean(types = {JwtDecoder.class, ChangeCartItemQuantity.class, RemoveFromCart.class})
class CartControllerTest {

    private final UserId customer = UserId.newId();
    private final ProductId beans = ProductId.newId();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ViewCart viewCart;

    @MockitoBean
    private AddToCart addToCart;

    @Test
    void customerAddsAnItemAndGetsTheUpdatedCart() throws Exception {
        CartView view = new CartView(
                List.of(new CartView.Line(beans, "Beans", Money.of("4.50"), 2, Money.of("9.00"), true)), Money.of("9.00"));
        when(viewCart.view(customer)).thenReturn(view);

        mockMvc.perform(post("/api/cart/items").with(asCustomer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": \"%s\", \"quantity\": 2}".formatted(beans)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].productName").value("Beans"))
                .andExpect(jsonPath("$.total").value(9.00));

        verify(addToCart).add(customer, beans, 2);
    }

    @Test
    void quantityZeroIsRejected() throws Exception {
        mockMvc.perform(post("/api/cart/items").with(asCustomer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": \"%s\", \"quantity\": 0}".formatted(beans)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void anonymousUserHasNoCart() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminHasNoCart() throws Exception {
        mockMvc.perform(get("/api/cart").with(asAdmin()))
                .andExpect(status().isForbidden());
    }
}
