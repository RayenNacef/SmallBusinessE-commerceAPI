package com.smallshop.catalog.application;

import com.smallshop.catalog.domain.Category;
import com.smallshop.catalog.domain.DuplicateCategoryNameException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCategory {

    private final CategoryRepository categories;

    public CreateCategory(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional
    public Category create(String name) {
        Category category = Category.create(name);
        if (categories.existsByName(category.name())) {
            throw new DuplicateCategoryNameException(category.name());
        }
        return categories.save(category);
    }
}
