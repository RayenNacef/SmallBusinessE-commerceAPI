package com.smallshop.inventory.domain;

import com.smallshop.shared.domain.BusinessConflictException;
import com.smallshop.shared.domain.ProductId;

public class InsufficientStockException extends BusinessConflictException {

    public InsufficientStockException(ProductId productId, int requested, int available) {
        super("Insufficient stock for product " + productId + ": requested " + requested + ", available " + available);
    }
}
