package com.smallshop.catalog.application;

import com.smallshop.catalog.domain.Product;
import com.smallshop.catalog.domain.ProductNotFoundException;
import com.smallshop.shared.domain.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeProductAvailability {

    private final ProductRepository products;

    public ChangeProductAvailability(ProductRepository products) {
        this.products = products;
    }

    @Transactional
    public Product deactivate(ProductId id) {
        Product product = load(id);
        product.deactivate();
        return products.save(product);
    }

    @Transactional
    public Product activate(ProductId id) {
        Product product = load(id);
        product.activate();
        return products.save(product);
    }

    private Product load(ProductId id) {
        return products.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }
}
