package com.smallshop.catalog.application;

import com.smallshop.catalog.domain.CategoryNotFoundException;
import com.smallshop.catalog.domain.Product;
import com.smallshop.inventory.application.InitializeStock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProduct {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final InitializeStock initializeStock;

    public CreateProduct(ProductRepository products, CategoryRepository categories, InitializeStock initializeStock) {
        this.products = products;
        this.categories = categories;
        this.initializeStock = initializeStock;
    }

    @Transactional
    public Product create(ProductDetails details, int initialStock) {
        if (!categories.existsById(details.categoryId())) {
            throw new CategoryNotFoundException(details.categoryId());
        }
        Product product = Product.create(details.name(), details.description(), details.price(), details.categoryId());
        Product saved = products.save(product);
        initializeStock.initialize(saved.id(), initialStock);
        return saved;
    }
}
