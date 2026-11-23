package com.smallshop.cart.domain;

import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartTest {

    private final Cart cart = Cart.emptyFor(UserId.newId());
    private final ProductId beans = ProductId.newId();
    private final ProductId tea = ProductId.newId();

    @Test
    void addingAProductPutsItInTheCart() {
        cart.add(beans, 2);

        assertThat(cart.items()).containsExactly(new CartItem(beans, 2));
    }

    @Test
    void addingTheSameProductTwiceMergesQuantities() {
        cart.add(beans, 2);
        cart.add(beans, 3);

        assertThat(cart.items()).containsExactly(new CartItem(beans, 5));
    }

    @Test
    void addingBeyondTheMaximumQuantityFails() {
        cart.add(beans, CartItem.MAX_QUANTITY);

        assertThatThrownBy(() -> cart.add(beans, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addingZeroUnitsFails() {
        assertThatThrownBy(() -> cart.add(beans, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changingQuantityReplacesIt() {
        cart.add(beans, 2);

        cart.changeQuantity(beans, 7);

        assertThat(cart.items()).containsExactly(new CartItem(beans, 7));
    }

    @Test
    void changingQuantityOfAProductNotInTheCartFails() {
        assertThatThrownBy(() -> cart.changeQuantity(tea, 1))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    @Test
    void removingAProductTakesOnlyThatProductOut() {
        cart.add(beans, 1);
        cart.add(tea, 1);

        cart.remove(beans);

        assertThat(cart.items()).containsExactly(new CartItem(tea, 1));
    }

    @Test
    void clearingEmptiesTheCart() {
        cart.add(beans, 1);

        cart.clear();

        assertThat(cart.isEmpty()).isTrue();
    }
}
