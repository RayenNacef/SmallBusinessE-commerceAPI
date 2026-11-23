package com.smallshop.inventory.application;

import com.smallshop.inventory.domain.StockItem;
import com.smallshop.inventory.domain.StockItemNotFoundException;
import com.smallshop.shared.domain.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StockReservation {

    private final StockItemRepository stockItems;

    public StockReservation(StockItemRepository stockItems) {
        this.stockItems = stockItems;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void reserve(Map<ProductId, Integer> quantities) {
        for (StockItem stockItem : lockAllOrFail(quantities)) {
            stockItem.reserve(quantities.get(stockItem.productId()));
            stockItems.save(stockItem);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void release(Map<ProductId, Integer> quantities) {
        for (StockItem stockItem : lockAllOrFail(quantities)) {
            stockItem.release(quantities.get(stockItem.productId()));
            stockItems.save(stockItem);
        }
    }

    private List<StockItem> lockAllOrFail(Map<ProductId, Integer> quantities) {
        List<StockItem> locked = stockItems.lockAll(quantities.keySet());
        Set<ProductId> lockedIds = new HashSet<>();
        for (StockItem stockItem : locked) {
            lockedIds.add(stockItem.productId());
        }
        for (ProductId productId : quantities.keySet()) {
            if (!lockedIds.contains(productId)) {
                throw new StockItemNotFoundException(productId);
            }
        }
        return locked;
    }
}
