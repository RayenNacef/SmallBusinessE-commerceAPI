package com.smallshop.inventory.application;

import com.smallshop.inventory.domain.InsufficientStockException;
import com.smallshop.inventory.domain.StockItem;
import com.smallshop.inventory.domain.StockItemNotFoundException;
import com.smallshop.shared.domain.ProductId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StockReservationTest {

    private final StockItemRepository stockItems = mock(StockItemRepository.class);
    private final StockReservation reservation = new StockReservation(stockItems);

    private final ProductId beans = ProductId.newId();
    private final ProductId tea = ProductId.newId();

    @Test
    void reservingTakesEachRequestedQuantityFromItsStock() {
        StockItem beansStock = new StockItem(beans, 10);
        StockItem teaStock = new StockItem(tea, 3);
        when(stockItems.lockAll(any())).thenReturn(List.of(beansStock, teaStock));

        reservation.reserve(Map.of(beans, 4, tea, 3));

        assertThat(beansStock.available()).isEqualTo(6);
        assertThat(teaStock.available()).isZero();
        verify(stockItems).save(beansStock);
        verify(stockItems).save(teaStock);
    }

    @Test
    void reservingMoreThanAvailableFails() {
        when(stockItems.lockAll(any())).thenReturn(List.of(new StockItem(beans, 1)));

        assertThatThrownBy(() -> reservation.reserve(Map.of(beans, 2)))
                .isInstanceOf(InsufficientStockException.class);
        verify(stockItems, never()).save(any());
    }

    @Test
    void reservingAProductWithoutStockRecordFails() {
        when(stockItems.lockAll(any())).thenReturn(List.of(new StockItem(beans, 5)));

        assertThatThrownBy(() -> reservation.reserve(Map.of(beans, 1, tea, 1)))
                .isInstanceOf(StockItemNotFoundException.class)
                .hasMessageContaining(tea.toString());
    }

    @Test
    void releasingPutsQuantitiesBack() {
        StockItem beansStock = new StockItem(beans, 0);
        when(stockItems.lockAll(any())).thenReturn(List.of(beansStock));

        reservation.release(Map.of(beans, 2));

        assertThat(beansStock.available()).isEqualTo(2);
    }
}
