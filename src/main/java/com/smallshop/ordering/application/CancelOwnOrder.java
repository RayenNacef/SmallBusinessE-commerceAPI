package com.smallshop.ordering.application;

import com.smallshop.inventory.application.StockReservation;
import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderId;
import com.smallshop.ordering.domain.OrderNotFoundException;
import com.smallshop.shared.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelOwnOrder {

    private final OrderRepository orders;
    private final StockReservation stockReservation;

    public CancelOwnOrder(OrderRepository orders, StockReservation stockReservation) {
        this.orders = orders;
        this.stockReservation = stockReservation;
    }

    @Transactional
    public Order cancel(UserId customerId, OrderId orderId) {
        Order order = orders.lockById(orderId)
                .filter(found -> found.isPlacedBy(customerId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.cancel();
        stockReservation.release(order.quantitiesByProduct());
        return orders.save(order);
    }
}
