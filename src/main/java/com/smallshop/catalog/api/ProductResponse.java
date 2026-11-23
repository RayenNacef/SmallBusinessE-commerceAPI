package com.smallshop.catalog.api;

import com.smallshop.catalog.domain.Product;

import java.math.BigDecimal;
import java.util.UUID;

record ProductResponse(UUID id, String name, String description, BigDecimal price, UUID categoryId, boolean active) {

    static ProductResponse from(Product product) {
        return new ProductResponse(
                product.id().value(),
                product.name(),
                product.description(),
                product.price().amount(),
                product.categoryId().value(),
                product.isActive());
    }
}
