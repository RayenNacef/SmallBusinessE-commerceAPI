package com.smallshop.cart.application;

import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveFromCart {

    private final CartRepository carts;

    public RemoveFromCart(CartRepository carts) {
        this.carts = carts;
    }

    @Transactional
    public void remove(UserId customerId, ProductId productId) {
        carts.findByCustomer(customerId).ifPresent(cart -> {
            cart.remove(productId);
            carts.save(cart);
        });
    }
}
