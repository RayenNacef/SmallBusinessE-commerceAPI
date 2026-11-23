package com.smallshop.ordering.domain;

import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;

import java.util.Objects;

// Name and price are copied at purchase time so later catalog changes never alter a placed order.
public record OrderLine(ProductId productId, String productName, Money unitPrice, int quantity) {

    public OrderLine {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(productName, "productName must not be null");
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
    }

    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }
}
