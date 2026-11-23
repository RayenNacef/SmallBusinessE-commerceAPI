package com.smallshop.ordering.application;

import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderId;
import com.smallshop.ordering.domain.OrderStatus;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import com.smallshop.shared.domain.UserId;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId id);

    Optional<Order> lockById(OrderId id);

    PageResult<Order> findByCustomer(UserId customerId, PageQuery pageQuery);

    PageResult<Order> findAll(Optional<OrderStatus> status, PageQuery pageQuery);
}
