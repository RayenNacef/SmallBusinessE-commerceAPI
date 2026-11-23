package com.smallshop.catalog.application;

import com.smallshop.catalog.domain.Product;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import com.smallshop.shared.domain.ProductId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(ProductId id);

    List<Product> findAllByIds(Collection<ProductId> ids);

    PageResult<Product> search(ProductSearchCriteria criteria, PageQuery pageQuery);
}
