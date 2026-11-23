package com.smallshop.cart.application;

import com.smallshop.cart.domain.Cart;
import com.smallshop.cart.domain.CartItem;
import com.smallshop.catalog.application.ProductLookup;
import com.smallshop.catalog.application.ProductSummary;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViewCartTest {

    private final CartRepository carts = mock(CartRepository.class);
    private final ProductLookup productLookup = mock(ProductLookup.class);
    private final ViewCart viewCart = new ViewCart(carts, productLookup);

    private final UserId customer = UserId.newId();
    private final ProductId beans = ProductId.newId();
    private final ProductId discontinued = ProductId.newId();

    @Test
    void cartShowsCurrentPricesAndExcludesUnavailableProductsFromTheTotal() {
        Cart cart = new Cart(customer, List.of(new CartItem(beans, 2), new CartItem(discontinued, 1)));
        when(carts.findByCustomer(customer)).thenReturn(Optional.of(cart));
        when(productLookup.findAll(any())).thenReturn(Map.of(
                beans, new ProductSummary(beans, "Beans", Money.of("4.50"), true),
                discontinued, new ProductSummary(discontinued, "Old blend", Money.of("3.00"), false)));

        CartView view = viewCart.view(customer);

        assertThat(view.lines()).hasSize(2);
        assertThat(view.lines().getFirst().lineTotal()).isEqualTo(Money.of("9.00"));
        assertThat(view.lines().get(1).available()).isFalse();
        assertThat(view.total()).isEqualTo(Money.of("9.00"));
    }

    @Test
    void customerWithoutCartSeesAnEmptyCart() {
        when(carts.findByCustomer(customer)).thenReturn(Optional.empty());
        when(productLookup.findAll(any())).thenReturn(Map.of());

        CartView view = viewCart.view(customer);

        assertThat(view.lines()).isEmpty();
        assertThat(view.total()).isEqualTo(Money.ZERO);
    }
}
