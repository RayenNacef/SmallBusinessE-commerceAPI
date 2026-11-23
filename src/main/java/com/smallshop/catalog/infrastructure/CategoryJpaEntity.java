package com.smallshop.catalog.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "categories")
class CategoryJpaEntity {

    @Id
    private UUID id;

    private String name;

    protected CategoryJpaEntity() {
    }

    CategoryJpaEntity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    UUID getId() {
        return id;
    }

    String getName() {
        return name;
    }
}
