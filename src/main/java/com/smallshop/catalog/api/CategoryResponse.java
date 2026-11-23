package com.smallshop.catalog.api;

import com.smallshop.catalog.domain.Category;

import java.util.UUID;

record CategoryResponse(UUID id, String name) {

    static CategoryResponse from(Category category) {
        return new CategoryResponse(category.id().value(), category.name());
    }
}
