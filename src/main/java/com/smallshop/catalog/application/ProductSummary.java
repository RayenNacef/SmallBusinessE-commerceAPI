package com.smallshop.catalog.application;

import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;

public record ProductSummary(ProductId id, String name, Money price, boolean active) {
}
