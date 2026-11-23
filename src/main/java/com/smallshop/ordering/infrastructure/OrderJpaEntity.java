package com.smallshop.ordering.infrastructure;

import com.smallshop.ordering.domain.OrderStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
class OrderJpaEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "placed_at")
    private Instant placedAt;

    // Batch fetching loads the lines of a whole page of orders in one query instead of one query per order.
    @ElementCollection
    @CollectionTable(name = "order_lines", joinColumns = @JoinColumn(name = "order_id"))
    @OrderColumn(name = "line_number")
    @BatchSize(size = 100)
    private List<OrderLineEmbeddable> lines = new ArrayList<>();

    protected OrderJpaEntity() {
    }

    OrderJpaEntity(UUID id, UUID customerId, OrderStatus status, Instant placedAt, List<OrderLineEmbeddable> lines) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.placedAt = placedAt;
        this.lines = new ArrayList<>(lines);
    }

    UUID getId() {
        return id;
    }

    UUID getCustomerId() {
        return customerId;
    }

    OrderStatus getStatus() {
        return status;
    }

    void setStatus(OrderStatus status) {
        this.status = status;
    }

    Instant getPlacedAt() {
        return placedAt;
    }

    List<OrderLineEmbeddable> getLines() {
        return lines;
    }
}
