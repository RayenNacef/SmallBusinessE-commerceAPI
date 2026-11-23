package com.smallshop.cart.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
class CartItemEmbeddable {

    @Column(name = "product_id")
    private UUID productId;

    private int quantity;

    protected CartItemEmbeddable() {
    }

    CartItemEmbeddable(UUID productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    UUID getProductId() {
        return productId;
    }

    int getQuantity() {
        return quantity;
    }
}
