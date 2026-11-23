package com.smallshop.catalog.application;

import com.smallshop.catalog.domain.CategoryId;
import com.smallshop.shared.domain.Money;

public record ProductDetails(String name, String description, Money price, CategoryId categoryId) {
}
