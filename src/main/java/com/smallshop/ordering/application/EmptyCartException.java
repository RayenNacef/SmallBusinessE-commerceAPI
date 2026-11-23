package com.smallshop.ordering.application;

import com.smallshop.shared.domain.BusinessConflictException;

public class EmptyCartException extends BusinessConflictException {

    public EmptyCartException() {
        super("Cannot place an order from an empty cart");
    }
}
