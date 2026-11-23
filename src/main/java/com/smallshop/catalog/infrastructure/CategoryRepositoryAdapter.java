package com.smallshop.catalog.infrastructure;

import com.smallshop.catalog.application.CategoryRepository;
import com.smallshop.catalog.domain.Category;
import com.smallshop.catalog.domain.CategoryId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpa;

    CategoryRepositoryAdapter(CategoryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Category save(Category category) {
        jpa.save(new CategoryJpaEntity(category.id().value(), category.name()));
        return category;
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return jpa.findById(id.value()).map(CategoryRepositoryAdapter::toDomain);
    }

    @Override
    public List<Category> findAllOrderedByName() {
        return jpa.findAllByOrderByNameAsc().stream().map(CategoryRepositoryAdapter::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return jpa.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsById(CategoryId id) {
        return jpa.existsById(id.value());
    }

    private static Category toDomain(CategoryJpaEntity entity) {
        return new Category(new CategoryId(entity.getId()), entity.getName());
    }
}
