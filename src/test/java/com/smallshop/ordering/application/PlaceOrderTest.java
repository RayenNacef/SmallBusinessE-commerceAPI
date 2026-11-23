package com.smallshop.ordering.application;

import com.smallshop.cart.application.CartCheckout;
import com.smallshop.cart.application.CartLine;
import com.smallshop.catalog.application.ProductLookup;
import com.smallshop.catalog.application.ProductSummary;
import com.smallshop.inventory.application.StockReservation;
import com.smallshop.inventory.domain.InsufficientStockException;
import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderLine;
import com.smallshop.ordering.domain.OrderStatus;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaceOrderTest {

    private static final Instant NOW = Instant.parse("2026-11-23T10:00:00Z");

    private final CartCheckout cartCheckout = mock(CartCheckout.class);
    private final ProductLookup productLookup = mock(ProductLookup.class);
    private final StockReservation stockReservation = mock(StockReservation.class);
    private final OrderRepository orders = mock(OrderRepository.class);
    private final PlaceOrder placeOrder = new PlaceOrder(
            cartCheckout, productLookup, stockReservation, orders, Clock.fixed(NOW, ZoneOffset.UTC));

    private final UserId customer = UserId.newId();
    private final ProductId beans = ProductId.newId();

    @Test
    void orderIsPricedAtTheCurrentCatalogPrice() {
        givenCart(new CartLine(beans, 2));
        givenProduct(new ProductSummary(beans, "Espresso beans", Money.of("12.50"), true));

        Order order = placeOrder.placeFromCart(customer);

        assertThat(order.lines()).containsExactly(new OrderLine(beans, "Espresso beans", Money.of("12.50"), 2));
        assertThat(order.total()).isEqualTo(Money.of("25.00"));
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.placedAt()).isEqualTo(NOW);
    }

    @Test
    void placingAnOrderReservesStockSavesTheOrderAndClearsTheCart() {
        givenCart(new CartLine(beans, 2));
        givenProduct(new ProductSummary(beans, "Espresso beans", Money.of("12.50"), true));

        Order order = placeOrder.placeFromCart(customer);

        verify(stockReservation).reserve(Map.of(beans, 2));
        verify(orders).save(order);
        verify(cartCheckout).clear(customer);
    }

    @Test
    void emptyCartCannotBeOrdered() {
        when(cartCheckout.linesOf(customer)).thenReturn(List.of());

        assertThatThrownBy(() -> placeOrder.placeFromCart(customer)).isInstanceOf(EmptyCartException.class);
        verify(stockReservation, never()).reserve(any());
    }

    @Test
    void deactivatedProductInTheCartBlocksTheOrder() {
        givenCart(new CartLine(beans, 1));
        givenProduct(new ProductSummary(beans, "Old blend", Money.of("9.00"), false));

        assertThatThrownBy(() -> placeOrder.placeFromCart(customer)).isInstanceOf(ProductUnavailableException.class);
        verify(stockReservation, never()).reserve(any());
        verify(orders, never()).save(any());
    }

    @Test
    void insufficientStockLeavesNoOrderAndKeepsTheCart() {
        givenCart(new CartLine(beans, 5));
        givenProduct(new ProductSummary(beans, "Espresso beans", Money.of("12.50"), true));
        doThrow(new InsufficientStockException(beans, 5, 1)).when(stockReservation).reserve(any());

        assertThatThrownBy(() -> placeOrder.placeFromCart(customer)).isInstanceOf(InsufficientStockException.class);
        verify(orders, never()).save(any());
        verify(cartCheckout, never()).clear(any());
    }

    private void givenCart(CartLine... lines) {
        when(cartCheckout.linesOf(customer)).thenReturn(List.of(lines));
    }

    private void givenProduct(ProductSummary product) {
        when(productLookup.findAll(any())).thenReturn(Map.of(product.id(), product));
    }
}
