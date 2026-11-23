package com.smallshop.catalog.application;

import com.smallshop.catalog.domain.Category;
import com.smallshop.catalog.domain.CategoryId;
import com.smallshop.catalog.domain.CategoryNotFoundException;
import com.smallshop.catalog.domain.DuplicateCategoryNameException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RenameCategory {

    private final CategoryRepository categories;

    public RenameCategory(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional
    public Category rename(CategoryId id, String newName) {
        Category category = categories.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
        String previousName = category.name();
        category.rename(newName);
        boolean nameChanged = !category.name().equalsIgnoreCase(previousName);
        if (nameChanged && categories.existsByName(category.name())) {
            throw new DuplicateCategoryNameException(category.name());
        }
        return categories.save(category);
    }
}
