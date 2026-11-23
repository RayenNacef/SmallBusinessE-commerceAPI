package com.smallshop.inventory.domain;

import com.smallshop.shared.domain.ProductId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockItemTest {

    private final ProductId productId = ProductId.newId();

    @Test
    void reservingTakesUnitsOutOfAvailableStock() {
        StockItem stock = new StockItem(productId, 5);

        stock.reserve(3);

        assertThat(stock.available()).isEqualTo(2);
    }

    @Test
    void reservingTheLastUnitsLeavesZero() {
        StockItem stock = new StockItem(productId, 2);

        stock.reserve(2);

        assertThat(stock.available()).isZero();
    }

    @Test
    void reservingMoreThanAvailableFailsAndLeavesStockUnchanged() {
        StockItem stock = new StockItem(productId, 2);

        assertThatThrownBy(() -> stock.reserve(3))
                .isInstanceOf(InsufficientStockException.class);
        assertThat(stock.available()).isEqualTo(2);
    }

    @Test
    void releasingPutsUnitsBack() {
        StockItem stock = new StockItem(productId, 0);

        stock.release(4);

        assertThat(stock.available()).isEqualTo(4);
    }

    @Test
    void reservingZeroUnitsIsRejected() {
        StockItem stock = new StockItem(productId, 5);

        assertThatThrownBy(() -> stock.reserve(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void stockCannotBeSetBelowZero() {
        StockItem stock = new StockItem(productId, 5);

        assertThatThrownBy(() -> stock.setAvailable(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
