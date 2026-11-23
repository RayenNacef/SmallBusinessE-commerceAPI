package com.smallshop.inventory.domain;

import com.smallshop.shared.domain.ProductId;

import java.util.Objects;

public class StockItem {

    private final ProductId productId;
    private int available;

    public StockItem(ProductId productId, int available) {
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.available = validQuantity(available);
    }

    public void setAvailable(int quantity) {
        this.available = validQuantity(quantity);
    }

    public void reserve(int quantity) {
        requirePositive(quantity);
        if (quantity > available) {
            throw new InsufficientStockException(productId, quantity, available);
        }
        available -= quantity;
    }

    public void release(int quantity) {
        requirePositive(quantity);
        available += quantity;
    }

    public ProductId productId() {
        return productId;
    }

    public int available() {
        return available;
    }

    private static int validQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity must not be negative: " + quantity);
        }
        return quantity;
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
    }
}
