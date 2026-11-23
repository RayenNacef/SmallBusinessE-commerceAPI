package com.smallshop.catalog.application;

import com.smallshop.catalog.domain.Category;
import com.smallshop.catalog.domain.CategoryId;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(CategoryId id);

    List<Category> findAllOrderedByName();

    boolean existsByName(String name);

    boolean existsById(CategoryId id);
}
