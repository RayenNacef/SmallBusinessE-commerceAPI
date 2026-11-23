package com.smallshop.ordering.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "PENDING, PAID",
            "PENDING, CANCELLED",
            "PAID, SHIPPED",
            "PAID, CANCELLED",
            "SHIPPED, DELIVERED"
    })
    void allowsTheNormalLifecycle(OrderStatus from, OrderStatus to) {
        assertThat(from.canMoveTo(to)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "PENDING, SHIPPED",
            "PENDING, DELIVERED",
            "PAID, PENDING",
            "SHIPPED, PENDING",
            "SHIPPED, PAID",
            "SHIPPED, CANCELLED",
            "DELIVERED, CANCELLED",
            "DELIVERED, PENDING",
            "CANCELLED, PENDING",
            "CANCELLED, PAID",
            "PAID, PAID"
    })
    void rejectsSkippingStepsGoingBackwardsAndLeavingFinalStates(OrderStatus from, OrderStatus to) {
        assertThat(from.canMoveTo(to)).isFalse();
    }
}
