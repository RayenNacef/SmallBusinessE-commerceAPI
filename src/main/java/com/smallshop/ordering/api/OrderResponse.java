package com.smallshop.ordering.api;

import com.smallshop.ordering.domain.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record OrderResponse(UUID id, UUID customerId, String status, Instant placedAt, List<Line> lines, BigDecimal total) {

    record Line(UUID productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
    }

    static OrderResponse from(Order order) {
        List<Line> lines = order.lines().stream()
                .map(line -> new Line(
                        line.productId().value(),
                        line.productName(),
                        line.unitPrice().amount(),
                        line.quantity(),
                        line.lineTotal().amount()))
                .toList();
        return new OrderResponse(
                order.id().value(),
                order.customerId().value(),
                order.status().name(),
                order.placedAt(),
                lines,
                order.total().amount());
    }
}
