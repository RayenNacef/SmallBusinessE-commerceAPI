package com.smallshop.catalog.api;

import com.smallshop.catalog.application.ChangeProductAvailability;
import com.smallshop.catalog.application.CreateProduct;
import com.smallshop.catalog.application.ManageProductListing;
import com.smallshop.catalog.application.UpdateProduct;
import com.smallshop.catalog.domain.CategoryId;
import com.smallshop.catalog.domain.Product;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import com.smallshop.shared.domain.ProductId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
class AdminProductController {

    private final CreateProduct createProduct;
    private final UpdateProduct updateProduct;
    private final ChangeProductAvailability changeAvailability;
    private final ManageProductListing productListing;

    AdminProductController(
            CreateProduct createProduct,
            UpdateProduct updateProduct,
            ChangeProductAvailability changeAvailability,
            ManageProductListing productListing) {
        this.createProduct = createProduct;
        this.updateProduct = updateProduct;
        this.changeAvailability = changeAvailability;
        this.productListing = productListing;
    }

    @PostMapping
    ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        Product product = createProduct.create(request.toDetails(), request.initialStock());
        URI location = URI.create("/api/admin/products/" + product.id());
        return ResponseEntity.created(location).body(ProductResponse.from(product));
    }

    @GetMapping
    PageResult<ProductResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam Optional<UUID> categoryId,
            @RequestParam Optional<String> search) {
        return productListing
                .listIncludingInactive(categoryId.map(CategoryId::new), search, new PageQuery(page, size))
                .map(ProductResponse::from);
    }

    @GetMapping("/{id}")
    ProductResponse get(@PathVariable UUID id) {
        return ProductResponse.from(productListing.get(new ProductId(id)));
    }

    @PutMapping("/{id}")
    ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return ProductResponse.from(updateProduct.update(new ProductId(id), request.toDetails()));
    }

    // Soft delete: orders keep referencing the product, it just disappears from the shop.
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        changeAvailability.deactivate(new ProductId(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    ProductResponse activate(@PathVariable UUID id) {
        return ProductResponse.from(changeAvailability.activate(new ProductId(id)));
    }
}
