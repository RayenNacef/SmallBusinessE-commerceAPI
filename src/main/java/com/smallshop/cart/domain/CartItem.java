package com.smallshop.cart.domain;

import com.smallshop.shared.domain.ProductId;

import java.util.Objects;

public record CartItem(ProductId productId, int quantity) {

    public static final int MAX_QUANTITY = 99;

    public CartItem {
        Objects.requireNonNull(productId, "productId must not be null");
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be between 1 and " + MAX_QUANTITY + ": " + quantity);
        }
    }
}
