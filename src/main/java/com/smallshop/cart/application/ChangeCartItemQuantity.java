package com.smallshop.cart.application;

import com.smallshop.cart.domain.Cart;
import com.smallshop.cart.domain.CartItemNotFoundException;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeCartItemQuantity {

    private final CartRepository carts;

    public ChangeCartItemQuantity(CartRepository carts) {
        this.carts = carts;
    }

    @Transactional
    public void change(UserId customerId, ProductId productId, int quantity) {
        Cart cart = carts.findByCustomer(customerId).orElseThrow(() -> new CartItemNotFoundException(productId));
        cart.changeQuantity(productId, quantity);
        carts.save(cart);
    }
}
