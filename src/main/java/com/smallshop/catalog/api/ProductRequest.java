package com.smallshop.catalog.api;

import com.smallshop.catalog.application.ProductDetails;
import com.smallshop.catalog.domain.CategoryId;
import com.smallshop.shared.domain.Money;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

record ProductRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull UUID categoryId) {

    ProductDetails toDetails() {
        return new ProductDetails(name, description, new Money(price), new CategoryId(categoryId));
    }
}
