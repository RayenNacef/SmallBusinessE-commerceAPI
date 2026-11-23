package com.smallshop.ordering.application;

import com.smallshop.inventory.application.StockReservation;
import com.smallshop.ordering.domain.InvalidOrderTransitionException;
import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderLine;
import com.smallshop.ordering.domain.OrderNotFoundException;
import com.smallshop.ordering.domain.OrderStatus;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
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

class CancelOwnOrderTest {

    private final OrderRepository orders = mock(OrderRepository.class);
    private final StockReservation stockReservation = mock(StockReservation.class);
    private final CancelOwnOrder cancelOwnOrder = new CancelOwnOrder(orders, stockReservation);

    private final UserId customer = UserId.newId();
    private final ProductId beans = ProductId.newId();
    private final Order order = Order.place(
            customer, List.of(new OrderLine(beans, "Beans", Money.of("5.00"), 3)), Instant.parse("2026-11-23T10:00:00Z"));

    @Test
    void cancellingReleasesTheReservedStock() {
        when(orders.lockById(order.id())).thenReturn(Optional.of(order));
        when(orders.save(order)).thenReturn(order);

        Order cancelled = cancelOwnOrder.cancel(customer, order.id());

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        verify(stockReservation).release(Map.of(beans, 3));
    }

    @Test
    void anotherCustomersOrderIsReportedAsNotFound() {
        when(orders.lockById(order.id())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> cancelOwnOrder.cancel(UserId.newId(), order.id()))
                .isInstanceOf(OrderNotFoundException.class);
        verify(stockReservation, never()).release(any());
    }

    @Test
    void cancellingAShippedOrderFailsAndReleasesNothing() {
        order.markPaid();
        order.ship();
        when(orders.lockById(order.id())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> cancelOwnOrder.cancel(customer, order.id()))
                .isInstanceOf(InvalidOrderTransitionException.class);
        verify(stockReservation, never()).release(any());
    }
}
