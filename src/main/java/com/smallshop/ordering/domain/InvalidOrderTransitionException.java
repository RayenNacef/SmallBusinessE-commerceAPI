package com.smallshop.ordering.domain;

import com.smallshop.shared.domain.BusinessConflictException;

public class InvalidOrderTransitionException extends BusinessConflictException {

    public InvalidOrderTransitionException(OrderId orderId, OrderStatus from, OrderStatus to) {
        super("Order " + orderId + " cannot move from " + from + " to " + to);
    }
}
