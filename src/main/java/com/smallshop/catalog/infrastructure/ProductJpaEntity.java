package com.smallshop.catalog.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
class ProductJpaEntity {

    @Id
    private UUID id;

    private String name;

    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "category_id")
    private UUID categoryId;

    private boolean active;

    protected ProductJpaEntity() {
    }

    ProductJpaEntity(UUID id, String name, String description, BigDecimal price, UUID categoryId, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.active = active;
    }

    UUID getId() {
        return id;
    }

    String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    BigDecimal getPrice() {
        return price;
    }

    UUID getCategoryId() {
        return categoryId;
    }

    boolean isActive() {
        return active;
    }
}
