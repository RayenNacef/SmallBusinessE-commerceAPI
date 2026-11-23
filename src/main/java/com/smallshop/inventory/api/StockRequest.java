package com.smallshop.inventory.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

record StockRequest(@NotNull @PositiveOrZero Integer available) {
}
