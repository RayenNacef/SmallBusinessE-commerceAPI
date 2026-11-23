package com.smallshop.cart.application;

import com.smallshop.shared.domain.ProductId;

public record CartLine(ProductId productId, int quantity) {
}
