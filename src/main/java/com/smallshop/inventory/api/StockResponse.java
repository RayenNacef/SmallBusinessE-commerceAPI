package com.smallshop.inventory.api;

import com.smallshop.inventory.domain.StockItem;

import java.util.UUID;

record StockResponse(UUID productId, int available) {

    static StockResponse from(StockItem stockItem) {
        return new StockResponse(stockItem.productId().value(), stockItem.available());
    }
}
