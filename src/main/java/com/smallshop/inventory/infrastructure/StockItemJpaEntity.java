package com.smallshop.inventory.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "stock_items")
class StockItemJpaEntity {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    private int available;

    protected StockItemJpaEntity() {
    }

    StockItemJpaEntity(UUID productId, int available) {
        this.productId = productId;
        this.available = available;
    }

    UUID getProductId() {
        return productId;
    }

    int getAvailable() {
        return available;
    }
}
