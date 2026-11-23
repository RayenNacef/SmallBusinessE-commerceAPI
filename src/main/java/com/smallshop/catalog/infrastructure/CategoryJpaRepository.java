package com.smallshop.catalog.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    boolean existsByNameIgnoreCase(String name);

    List<CategoryJpaEntity> findAllByOrderByNameAsc();
}
