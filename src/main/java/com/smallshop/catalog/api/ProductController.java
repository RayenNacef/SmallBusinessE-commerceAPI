package com.smallshop.catalog.api;

import com.smallshop.catalog.application.BrowseProducts;
import com.smallshop.catalog.domain.CategoryId;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import com.smallshop.shared.domain.ProductId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
class ProductController {

    private final BrowseProducts browseProducts;

    ProductController(BrowseProducts browseProducts) {
        this.browseProducts = browseProducts;
    }

    @GetMapping
    PageResult<ProductResponse> browse(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam Optional<UUID> categoryId,
            @RequestParam Optional<String> search) {
        return browseProducts
                .browse(categoryId.map(CategoryId::new), search, new PageQuery(page, size))
                .map(ProductResponse::from);
    }

    @GetMapping("/{id}")
    ProductResponse get(@PathVariable UUID id) {
        return ProductResponse.from(browseProducts.getActive(new ProductId(id)));
    }
}
