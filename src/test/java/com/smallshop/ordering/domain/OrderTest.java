package com.smallshop.ordering.domain;

import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Instant NOW = Instant.parse("2026-11-23T10:00:00Z");

    private final UserId customer = UserId.newId();
    private final ProductId beans = ProductId.newId();
    private final ProductId tea = ProductId.newId();

    @Test
    void newOrderIsPending() {
        assertThat(anOrder().status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void totalIsTheSumOfLineTotals() {
        Order order = Order.place(customer, List.of(
                new OrderLine(beans, "Beans", Money.of("12.50"), 2),
                new OrderLine(tea, "Tea", Money.of("3.99"), 3)), NOW);

        assertThat(order.total()).isEqualTo(Money.of("36.97"));
    }

    @Test
    void lineKeepsThePriceItWasPlacedAt() {
        Money priceAtPurchase = Money.of("12.50");
        Order order = Order.place(customer, List.of(new OrderLine(beans, "Beans", priceAtPurchase, 1)), NOW);

        assertThat(order.lines().getFirst().unitPrice()).isEqualTo(priceAtPurchase);
        assertThat(order.total()).isEqualTo(priceAtPurchase);
    }

    @Test
    void orderWithoutLinesIsRejected() {
        assertThatThrownBy(() -> Order.place(customer, List.of(), NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sameProductOnTwoLinesIsRejected() {
        List<OrderLine> lines = List.of(
                new OrderLine(beans, "Beans", Money.of("1.00"), 1),
                new OrderLine(beans, "Beans", Money.of("1.00"), 2));

        assertThatThrownBy(() -> Order.place(customer, lines, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void orderGoesThroughTheFullLifecycle() {
        Order order = anOrder();

        order.markPaid();
        order.ship();
        order.deliver();

        assertThat(order.status()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void pendingOrderCanBeCancelled() {
        Order order = anOrder();

        order.cancel();

        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancellingAShippedOrderFails() {
        Order order = anOrder();
        order.markPaid();
        order.ship();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(InvalidOrderTransitionException.class)
                .hasMessageContaining("SHIPPED")
                .hasMessageContaining("CANCELLED");
        assertThat(order.status()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void cancellingADeliveredOrderFails() {
        Order order = anOrder();
        order.markPaid();
        order.ship();
        order.deliver();

        assertThatThrownBy(order::cancel).isInstanceOf(InvalidOrderTransitionException.class);
    }

    @Test
    void shippingAnUnpaidOrderFails() {
        assertThatThrownBy(anOrder()::ship).isInstanceOf(InvalidOrderTransitionException.class);
    }

    @Test
    void cancellingTwiceFails() {
        Order order = anOrder();
        order.cancel();

        assertThatThrownBy(order::cancel).isInstanceOf(InvalidOrderTransitionException.class);
    }

    @Test
    void quantitiesByProductListsWhatToReserveOrRelease() {
        Order order = Order.place(customer, List.of(
                new OrderLine(beans, "Beans", Money.of("1.00"), 2),
                new OrderLine(tea, "Tea", Money.of("1.00"), 5)), NOW);

        assertThat(order.quantitiesByProduct()).isEqualTo(Map.of(beans, 2, tea, 5));
    }

    private Order anOrder() {
        return Order.place(customer, List.of(new OrderLine(beans, "Beans", Money.of("10.00"), 1)), NOW);
    }
}
