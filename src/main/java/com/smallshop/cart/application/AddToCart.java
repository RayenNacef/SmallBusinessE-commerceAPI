package com.smallshop.cart.application;

import com.smallshop.cart.domain.Cart;
import com.smallshop.catalog.application.ProductLookup;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddToCart {

    private final CartRepository carts;
    private final ProductLookup productLookup;

    public AddToCart(CartRepository carts, ProductLookup productLookup) {
        this.carts = carts;
        this.productLookup = productLookup;
    }

    @Transactional
    public void add(UserId customerId, ProductId productId, int quantity) {
        productLookup.requireActive(productId);
        Cart cart = carts.findByCustomer(customerId).orElseGet(() -> Cart.emptyFor(customerId));
        cart.add(productId, quantity);
        carts.save(cart);
    }
}
