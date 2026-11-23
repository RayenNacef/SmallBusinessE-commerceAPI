package com.smallshop.catalog.application;

import com.smallshop.catalog.domain.CategoryNotFoundException;
import com.smallshop.catalog.domain.Product;
import com.smallshop.catalog.domain.ProductNotFoundException;
import com.smallshop.shared.domain.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProduct {

    private final ProductRepository products;
    private final CategoryRepository categories;

    public UpdateProduct(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    @Transactional
    public Product update(ProductId id, ProductDetails details) {
        Product product = products.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        if (!categories.existsById(details.categoryId())) {
            throw new CategoryNotFoundException(details.categoryId());
        }
        product.updateDetails(details.name(), details.description(), details.price(), details.categoryId());
        return products.save(product);
    }
}
