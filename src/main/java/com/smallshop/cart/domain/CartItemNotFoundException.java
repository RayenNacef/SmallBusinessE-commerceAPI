package com.smallshop.cart.domain;

import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.ResourceNotFoundException;

public class CartItemNotFoundException extends ResourceNotFoundException {

    public CartItemNotFoundException(ProductId productId) {
        super("Product " + productId + " is not in the cart");
    }
}
