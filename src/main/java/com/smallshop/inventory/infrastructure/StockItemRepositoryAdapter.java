package com.smallshop.inventory.infrastructure;

import com.smallshop.inventory.application.StockItemRepository;
import com.smallshop.inventory.domain.StockItem;
import com.smallshop.shared.domain.ProductId;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class StockItemRepositoryAdapter implements StockItemRepository {

    private final StockItemJpaRepository jpa;

    StockItemRepositoryAdapter(StockItemJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public StockItem save(StockItem stockItem) {
        jpa.save(new StockItemJpaEntity(stockItem.productId().value(), stockItem.available()));
        return stockItem;
    }

    @Override
    public Optional<StockItem> findById(ProductId productId) {
        return jpa.findById(productId.value()).map(StockItemRepositoryAdapter::toDomain);
    }

    @Override
    public List<StockItem> lockAll(Collection<ProductId> productIds) {
        List<UUID> rawIds = productIds.stream().map(ProductId::value).toList();
        return jpa.findAllForUpdate(rawIds).stream().map(StockItemRepositoryAdapter::toDomain).toList();
    }

    private static StockItem toDomain(StockItemJpaEntity entity) {
        return new StockItem(new ProductId(entity.getProductId()), entity.getAvailable());
    }
}
