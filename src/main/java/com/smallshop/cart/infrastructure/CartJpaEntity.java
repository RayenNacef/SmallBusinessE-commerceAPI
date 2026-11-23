package com.smallshop.cart.infrastructure;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
class CartJpaEntity {

    @Id
    @Column(name = "customer_id")
    private UUID customerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "customer_id"))
    private List<CartItemEmbeddable> items = new ArrayList<>();

    protected CartJpaEntity() {
    }

    CartJpaEntity(UUID customerId, List<CartItemEmbeddable> items) {
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
    }

    UUID getCustomerId() {
        return customerId;
    }

    List<CartItemEmbeddable> getItems() {
        return items;
    }
}
