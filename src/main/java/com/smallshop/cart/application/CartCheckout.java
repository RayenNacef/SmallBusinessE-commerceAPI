package com.smallshop.cart.application;

import com.smallshop.cart.domain.Cart;
import com.smallshop.cart.domain.CartItem;
import com.smallshop.shared.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class CartCheckout {

    private final CartRepository carts;

    public CartCheckout(CartRepository carts) {
        this.carts = carts;
    }

    @Transactional(readOnly = true)
    public List<CartLine> linesOf(UserId customerId) {
        return carts.findByCustomer(customerId)
                .map(Cart::items)
                .orElse(List.of())
                .stream()
                .map(CartCheckout::toLine)
                .toList();
    }

    @Transactional
    public void clear(UserId customerId) {
        carts.findByCustomer(customerId).ifPresent(cart -> {
            cart.clear();
            carts.save(cart);
        });
    }

    private static CartLine toLine(CartItem item) {
        return new CartLine(item.productId(), item.quantity());
    }
}
