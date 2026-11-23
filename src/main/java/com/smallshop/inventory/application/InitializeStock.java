package com.smallshop.inventory.application;

import com.smallshop.inventory.domain.StockItem;
import com.smallshop.shared.domain.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InitializeStock {

    private final StockItemRepository stockItems;

    public InitializeStock(StockItemRepository stockItems) {
        this.stockItems = stockItems;
    }

    @Transactional
    public void initialize(ProductId productId, int quantity) {
        stockItems.save(new StockItem(productId, quantity));
    }
}
