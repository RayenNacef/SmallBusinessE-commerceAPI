package com.smallshop.ordering.domain;

import com.smallshop.shared.domain.ResourceNotFoundException;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(OrderId id) {
        super("Order not found: " + id);
    }
}
