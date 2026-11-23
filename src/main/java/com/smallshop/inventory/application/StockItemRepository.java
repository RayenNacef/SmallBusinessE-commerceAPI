package com.smallshop.inventory.application;

import com.smallshop.inventory.domain.StockItem;
import com.smallshop.shared.domain.ProductId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockItemRepository {

    StockItem save(StockItem stockItem);

    Optional<StockItem> findById(ProductId productId);

    List<StockItem> lockAll(Collection<ProductId> productIds);
}
