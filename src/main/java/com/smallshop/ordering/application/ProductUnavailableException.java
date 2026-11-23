package com.smallshop.ordering.application;

import com.smallshop.shared.domain.BusinessConflictException;
import com.smallshop.shared.domain.ProductId;

public class ProductUnavailableException extends BusinessConflictException {

    public ProductUnavailableException(ProductId productId) {
        super("Product " + productId + " is no longer available; remove it from the cart to continue");
    }
}
