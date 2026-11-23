package com.smallshop.catalog.application;

import com.smallshop.catalog.domain.CategoryId;
import com.smallshop.catalog.domain.Product;
import com.smallshop.catalog.domain.ProductNotFoundException;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import com.smallshop.shared.domain.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BrowseProducts {

    private final ProductRepository products;

    public BrowseProducts(ProductRepository products) {
        this.products = products;
    }

    @Transactional(readOnly = true)
    public PageResult<Product> browse(Optional<CategoryId> categoryId, Optional<String> search, PageQuery pageQuery) {
        return products.search(new ProductSearchCriteria(categoryId, search, true), pageQuery);
    }

    @Transactional(readOnly = true)
    public Product getActive(ProductId id) {
        return products.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
