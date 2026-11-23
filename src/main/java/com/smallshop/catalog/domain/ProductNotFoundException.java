package com.smallshop.catalog.domain;

import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(ProductId id) {
        super("Product not found: " + id);
    }
}
