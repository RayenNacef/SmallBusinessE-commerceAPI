package com.smallshop.catalog.application;

import com.smallshop.catalog.domain.Product;
import com.smallshop.catalog.domain.ProductNotFoundException;
import com.smallshop.shared.domain.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProductLookup {

    private final ProductRepository products;

    public ProductLookup(ProductRepository products) {
        this.products = products;
    }

    @Transactional(readOnly = true)
    public ProductSummary requireActive(ProductId id) {
        return products.findById(id)
                .filter(Product::isActive)
                .map(ProductLookup::toSummary)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Map<ProductId, ProductSummary> findAll(Collection<ProductId> ids) {
        Map<ProductId, ProductSummary> summaries = new HashMap<>();
        for (Product product : products.findAllByIds(ids)) {
            summaries.put(product.id(), toSummary(product));
        }
        return summaries;
    }

    private static ProductSummary toSummary(Product product) {
        return new ProductSummary(product.id(), product.name(), product.price(), product.isActive());
    }
}
