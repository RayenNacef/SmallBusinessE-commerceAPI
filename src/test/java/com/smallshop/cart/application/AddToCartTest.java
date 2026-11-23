package com.smallshop.cart.application;

import com.smallshop.cart.domain.Cart;
import com.smallshop.cart.domain.CartItem;
import com.smallshop.catalog.application.ProductLookup;
import com.smallshop.catalog.application.ProductSummary;
import com.smallshop.catalog.domain.ProductNotFoundException;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AddToCartTest {

    private final CartRepository carts = mock(CartRepository.class);
    private final ProductLookup productLookup = mock(ProductLookup.class);
    private final AddToCart addToCart = new AddToCart(carts, productLookup);

    private final UserId customer = UserId.newId();
    private final ProductId beans = ProductId.newId();

    @Test
    void firstItemCreatesTheCart() {
        when(productLookup.requireActive(beans)).thenReturn(new ProductSummary(beans, "Beans", Money.of("5.00"), true));
        when(carts.findByCustomer(customer)).thenReturn(Optional.empty());

        addToCart.add(customer, beans, 2);

        ArgumentCaptor<Cart> saved = ArgumentCaptor.forClass(Cart.class);
        verify(carts).save(saved.capture());
        assertThat(saved.getValue().customerId()).isEqualTo(customer);
        assertThat(saved.getValue().items()).containsExactly(new CartItem(beans, 2));
    }

    @Test
    void addingToAnExistingCartMergesQuantities() {
        when(productLookup.requireActive(beans)).thenReturn(new ProductSummary(beans, "Beans", Money.of("5.00"), true));
        Cart existing = new Cart(customer, List.of(new CartItem(beans, 1)));
        when(carts.findByCustomer(customer)).thenReturn(Optional.of(existing));

        addToCart.add(customer, beans, 2);

        assertThat(existing.items()).containsExactly(new CartItem(beans, 3));
    }

    @Test
    void inactiveOrUnknownProductCannotBeAdded() {
        when(productLookup.requireActive(beans)).thenThrow(new ProductNotFoundException(beans));

        assertThatThrownBy(() -> addToCart.add(customer, beans, 1))
                .isInstanceOf(ProductNotFoundException.class);
        verify(carts, never()).save(any());
    }
}
