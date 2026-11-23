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
public class ManageProductListing {

    private final ProductRepository products;

    public ManageProductListing(ProductRepository products) {
        this.products = products;
    }

    @Transactional(readOnly = true)
    public PageResult<Product> listIncludingInactive(Optional<CategoryId> categoryId, Optional<String> search, PageQuery pageQuery) {
        return products.search(new ProductSearchCriteria(categoryId, search, false), pageQuery);
    }

    @Transactional(readOnly = true)
    public Product get(ProductId id) {
        return products.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }
}
