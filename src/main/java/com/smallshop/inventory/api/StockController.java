package com.smallshop.inventory.api;

import com.smallshop.inventory.application.ManageStockLevel;
import com.smallshop.shared.domain.ProductId;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products/{productId}/stock")
class StockController {

    private final ManageStockLevel manageStockLevel;

    StockController(ManageStockLevel manageStockLevel) {
        this.manageStockLevel = manageStockLevel;
    }

    @GetMapping
    StockResponse get(@PathVariable UUID productId) {
        return StockResponse.from(manageStockLevel.get(new ProductId(productId)));
    }

    @PutMapping
    StockResponse set(@PathVariable UUID productId, @Valid @RequestBody StockRequest request) {
        return StockResponse.from(manageStockLevel.set(new ProductId(productId), request.available()));
    }
}
