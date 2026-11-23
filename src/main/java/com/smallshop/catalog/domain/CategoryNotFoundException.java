package com.smallshop.catalog.domain;

import com.smallshop.shared.domain.ResourceNotFoundException;

public class CategoryNotFoundException extends ResourceNotFoundException {

    public CategoryNotFoundException(CategoryId id) {
        super("Category not found: " + id);
    }
}
