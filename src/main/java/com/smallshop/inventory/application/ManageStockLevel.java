package com.smallshop.inventory.application;

import com.smallshop.inventory.domain.StockItem;
import com.smallshop.inventory.domain.StockItemNotFoundException;
import com.smallshop.shared.domain.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ManageStockLevel {

    private final StockItemRepository stockItems;

    public ManageStockLevel(StockItemRepository stockItems) {
        this.stockItems = stockItems;
    }

    @Transactional(readOnly = true)
    public StockItem get(ProductId productId) {
        return stockItems.findById(productId).orElseThrow(() -> new StockItemNotFoundException(productId));
    }

    @Transactional
    public StockItem set(ProductId productId, int quantity) {
        // Locked like a reservation so a stock correction cannot interleave with a checkout on the same row.
        List<StockItem> locked = stockItems.lockAll(List.of(productId));
        if (locked.isEmpty()) {
            throw new StockItemNotFoundException(productId);
        }
        StockItem stockItem = locked.getFirst();
        stockItem.setAvailable(quantity);
        return stockItems.save(stockItem);
    }
}
