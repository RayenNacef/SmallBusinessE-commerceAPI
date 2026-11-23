package com.smallshop.cart.application;

import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;

import java.util.List;

public record CartView(List<Line> lines, Money total) {

    public record Line(ProductId productId, String productName, Money unitPrice, int quantity, Money lineTotal, boolean available) {
    }
}
