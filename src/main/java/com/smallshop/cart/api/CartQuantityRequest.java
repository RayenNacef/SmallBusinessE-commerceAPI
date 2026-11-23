package com.smallshop.cart.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

record CartQuantityRequest(@NotNull @Min(1) @Max(99) Integer quantity) {
}
