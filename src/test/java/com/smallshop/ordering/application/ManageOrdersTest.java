package com.smallshop.ordering.application;

import com.smallshop.inventory.application.StockReservation;
import com.smallshop.ordering.domain.InvalidOrderTransitionException;
import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderLine;
import com.smallshop.ordering.domain.OrderStatus;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManageOrdersTest {

    private final OrderRepository orders = mock(OrderRepository.class);
    private final StockReservation stockReservation = mock(StockReservation.class);
    private final ManageOrders manageOrders = new ManageOrders(orders, stockReservation);

    private final ProductId beans = ProductId.newId();
    private final Order order = Order.place(
            UserId.newId(), List.of(new OrderLine(beans, "Beans", Money.of("5.00"), 2)), Instant.parse("2026-11-23T10:00:00Z"));

    @BeforeEach
    void setUp() {
        when(orders.lockById(order.id())).thenReturn(Optional.of(order));
        when(orders.save(order)).thenReturn(order);
    }

    @Test
    void markingAsPaidDoesNotTouchStock() {
        Order paid = manageOrders.moveTo(order.id(), OrderStatus.PAID);

        assertThat(paid.status()).isEqualTo(OrderStatus.PAID);
        verify(stockReservation, never()).release(any());
    }

    @Test
    void adminCancellationReleasesStock() {
        manageOrders.moveTo(order.id(), OrderStatus.PAID);

        Order cancelled = manageOrders.moveTo(order.id(), OrderStatus.CANCELLED);

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        verify(stockReservation).release(Map.of(beans, 2));
    }

    @Test
    void shippingAPendingOrderFails() {
        assertThatThrownBy(() -> manageOrders.moveTo(order.id(), OrderStatus.SHIPPED))
                .isInstanceOf(InvalidOrderTransitionException.class);
    }
}
