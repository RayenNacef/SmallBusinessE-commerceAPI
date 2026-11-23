package com.smallshop.catalog.infrastructure;

import com.smallshop.catalog.application.ProductRepository;
import com.smallshop.catalog.application.ProductSearchCriteria;
import com.smallshop.catalog.domain.CategoryId;
import com.smallshop.catalog.domain.Product;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class ProductRepositoryAdapter implements ProductRepository {

    private static final Sort BROWSE_ORDER = Sort.by("name").and(Sort.by("id"));

    private final ProductJpaRepository jpa;

    ProductRepositoryAdapter(ProductJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Product save(Product product) {
        jpa.save(toEntity(product));
        return product;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpa.findById(id.value()).map(ProductRepositoryAdapter::toDomain);
    }

    @Override
    public List<Product> findAllByIds(Collection<ProductId> ids) {
        List<UUID> rawIds = ids.stream().map(ProductId::value).toList();
        return jpa.findAllById(rawIds).stream().map(ProductRepositoryAdapter::toDomain).toList();
    }

    @Override
    public PageResult<Product> search(ProductSearchCriteria criteria, PageQuery pageQuery) {
        PageRequest pageRequest = PageRequest.of(pageQuery.page(), pageQuery.size(), BROWSE_ORDER);
        Page<ProductJpaEntity> page = jpa.findAll(ProductSpecifications.matching(criteria), pageRequest);
        List<Product> products = page.getContent().stream().map(ProductRepositoryAdapter::toDomain).toList();
        return new PageResult<>(products, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private static ProductJpaEntity toEntity(Product product) {
        return new ProductJpaEntity(
                product.id().value(),
                product.name(),
                product.description(),
                product.price().amount(),
                product.categoryId().value(),
                product.isActive());
    }

    private static Product toDomain(ProductJpaEntity entity) {
        return new Product(
                new ProductId(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                new Money(entity.getPrice()),
                new CategoryId(entity.getCategoryId()),
                entity.isActive());
    }
}
