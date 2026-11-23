package com.smallshop.cart.api;

import com.smallshop.cart.application.CartView;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

record CartResponse(List<Line> lines, BigDecimal total) {

    record Line(UUID productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal, boolean available) {
    }

    static CartResponse from(CartView view) {
        List<Line> lines = view.lines().stream()
                .map(line -> new Line(
                        line.productId().value(),
                        line.productName(),
                        line.unitPrice().amount(),
                        line.quantity(),
                        line.lineTotal().amount(),
                        line.available()))
                .toList();
        return new CartResponse(lines, view.total().amount());
    }
}
