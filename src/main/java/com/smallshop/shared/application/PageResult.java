package com.smallshop.shared.application;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public PageResult {
        items = List.copyOf(items);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        List<R> mapped = items.stream().map(mapper).toList();
        return new PageResult<>(mapped, page, size, totalItems, totalPages);
    }
}
