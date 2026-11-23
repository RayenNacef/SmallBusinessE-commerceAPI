package com.smallshop.catalog.application;

import com.smallshop.catalog.domain.CategoryId;

import java.util.Optional;

public record ProductSearchCriteria(Optional<CategoryId> categoryId, Optional<String> nameContains, boolean activeOnly) {

    public ProductSearchCriteria {
        nameContains = nameContains.map(String::trim).filter(text -> !text.isEmpty());
    }
}
