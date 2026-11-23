package com.smallshop.inventory.domain;

import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.ResourceNotFoundException;

public class StockItemNotFoundException extends ResourceNotFoundException {

    public StockItemNotFoundException(ProductId productId) {
        super("No stock record for product " + productId);
    }
}
