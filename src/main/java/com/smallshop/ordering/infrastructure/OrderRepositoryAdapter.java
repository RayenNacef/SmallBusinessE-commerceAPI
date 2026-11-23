package com.smallshop.ordering.infrastructure;

import com.smallshop.ordering.application.OrderRepository;
import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderId;
import com.smallshop.ordering.domain.OrderLine;
import com.smallshop.ordering.domain.OrderStatus;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class OrderRepositoryAdapter implements OrderRepository {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "placedAt").and(Sort.by("id"));

    private final OrderJpaRepository jpa;

    OrderRepositoryAdapter(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    // Lines never change after placing, so an existing order only gets its status updated.
    @Override
    public Order save(Order order) {
        Optional<OrderJpaEntity> existing = jpa.findById(order.id().value());
        if (existing.isPresent()) {
            existing.get().setStatus(order.status());
        } else {
            jpa.save(toEntity(order));
        }
        return order;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpa.findById(id.value()).map(OrderRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<Order> lockById(OrderId id) {
        return jpa.findByIdForUpdate(id.value()).map(OrderRepositoryAdapter::toDomain);
    }

    @Override
    public PageResult<Order> findByCustomer(UserId customerId, PageQuery pageQuery) {
        return toPageResult(jpa.findByCustomerId(customerId.value(), pageRequest(pageQuery)));
    }

    @Override
    public PageResult<Order> findAll(Optional<OrderStatus> status, PageQuery pageQuery) {
        PageRequest pageRequest = pageRequest(pageQuery);
        Page<OrderJpaEntity> page = status.isPresent()
                ? jpa.findByStatus(status.get(), pageRequest)
                : jpa.findAll(pageRequest);
        return toPageResult(page);
    }

    private static PageRequest pageRequest(PageQuery pageQuery) {
        return PageRequest.of(pageQuery.page(), pageQuery.size(), NEWEST_FIRST);
    }

    private static PageResult<Order> toPageResult(Page<OrderJpaEntity> page) {
        List<Order> orders = page.getContent().stream().map(OrderRepositoryAdapter::toDomain).toList();
        return new PageResult<>(orders, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private static OrderJpaEntity toEntity(Order order) {
        List<OrderLineEmbeddable> lines = order.lines().stream()
                .map(line -> new OrderLineEmbeddable(
                        line.productId().value(), line.productName(), line.unitPrice().amount(), line.quantity()))
                .toList();
        return new OrderJpaEntity(order.id().value(), order.customerId().value(), order.status(), order.placedAt(), lines);
    }

    private static Order toDomain(OrderJpaEntity entity) {
        List<OrderLine> lines = entity.getLines().stream()
                .map(line -> new OrderLine(
                        new ProductId(line.getProductId()),
                        line.getProductName(),
                        new Money(line.getUnitPrice()),
                        line.getQuantity()))
                .toList();
        return new Order(
                new OrderId(entity.getId()),
                new UserId(entity.getCustomerId()),
                lines,
                entity.getStatus(),
                entity.getPlacedAt());
    }
}
