package com.smallshop.ordering.domain;

import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Order {

    private final OrderId id;
    private final UserId customerId;
    private final List<OrderLine> lines;
    private final Instant placedAt;
    private OrderStatus status;

    public Order(OrderId id, UserId customerId, List<OrderLine> lines, OrderStatus status, Instant placedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        this.lines = validLines(lines);
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.placedAt = Objects.requireNonNull(placedAt, "placedAt must not be null");
    }

    public static Order place(UserId customerId, List<OrderLine> lines, Instant placedAt) {
        return new Order(OrderId.newId(), customerId, lines, OrderStatus.PENDING, placedAt);
    }

    public void markPaid() {
        moveTo(OrderStatus.PAID);
    }

    public void ship() {
        moveTo(OrderStatus.SHIPPED);
    }

    public void deliver() {
        moveTo(OrderStatus.DELIVERED);
    }

    public void cancel() {
        moveTo(OrderStatus.CANCELLED);
    }

    public Money total() {
        Money total = Money.ZERO;
        for (OrderLine line : lines) {
            total = total.add(line.lineTotal());
        }
        return total;
    }

    public Map<ProductId, Integer> quantitiesByProduct() {
        Map<ProductId, Integer> quantities = new LinkedHashMap<>();
        for (OrderLine line : lines) {
            quantities.put(line.productId(), line.quantity());
        }
        return quantities;
    }

    public boolean isPlacedBy(UserId customer) {
        return customerId.equals(customer);
    }

    public OrderId id() {
        return id;
    }

    public UserId customerId() {
        return customerId;
    }

    public List<OrderLine> lines() {
        return lines;
    }

    public OrderStatus status() {
        return status;
    }

    public Instant placedAt() {
        return placedAt;
    }

    private void moveTo(OrderStatus target) {
        if (!status.canMoveTo(target)) {
            throw new InvalidOrderTransitionException(id, status, target);
        }
        status = target;
    }

    private static List<OrderLine> validLines(List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one line");
        }
        Set<ProductId> seen = new HashSet<>();
        for (OrderLine line : lines) {
            if (!seen.add(line.productId())) {
                throw new IllegalArgumentException("Product " + line.productId() + " appears on more than one line");
            }
        }
        return List.copyOf(lines);
    }
}
