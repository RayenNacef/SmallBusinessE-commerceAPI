package com.smallshop.ordering.application;

import com.smallshop.inventory.application.StockReservation;
import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderId;
import com.smallshop.ordering.domain.OrderNotFoundException;
import com.smallshop.ordering.domain.OrderStatus;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ManageOrders {

    private final OrderRepository orders;
    private final StockReservation stockReservation;

    public ManageOrders(OrderRepository orders, StockReservation stockReservation) {
        this.orders = orders;
        this.stockReservation = stockReservation;
    }

    @Transactional(readOnly = true)
    public PageResult<Order> list(Optional<OrderStatus> status, PageQuery pageQuery) {
        return orders.findAll(status, pageQuery);
    }

    @Transactional(readOnly = true)
    public Order get(OrderId orderId) {
        return orders.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional
    public Order moveTo(OrderId orderId, OrderStatus target) {
        Order order = orders.lockById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        switch (target) {
            case PAID -> order.markPaid();
            case SHIPPED -> order.ship();
            case DELIVERED -> order.deliver();
            case CANCELLED -> {
                order.cancel();
                stockReservation.release(order.quantitiesByProduct());
            }
            case PENDING -> throw new IllegalArgumentException("An order cannot be moved back to PENDING");
        }
        return orders.save(order);
    }
}
