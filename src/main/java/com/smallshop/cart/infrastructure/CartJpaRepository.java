package com.smallshop.cart.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CartJpaRepository extends JpaRepository<CartJpaEntity, UUID> {
}
