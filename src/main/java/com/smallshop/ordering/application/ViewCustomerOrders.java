package com.smallshop.ordering.application;

import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderId;
import com.smallshop.ordering.domain.OrderNotFoundException;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import com.smallshop.shared.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewCustomerOrders {

    private final OrderRepository orders;

    public ViewCustomerOrders(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public PageResult<Order> history(UserId customerId, PageQuery pageQuery) {
        return orders.findByCustomer(customerId, pageQuery);
    }

    // Another customer's order is reported as not found rather than forbidden, so order ids cannot be probed.
    @Transactional(readOnly = true)
    public Order get(UserId customerId, OrderId orderId) {
        return orders.findById(orderId)
                .filter(order -> order.isPlacedBy(customerId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
