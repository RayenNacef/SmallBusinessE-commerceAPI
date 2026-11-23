package com.smallshop.catalog.api;

import com.smallshop.catalog.application.ListCategories;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
class CategoryController {

    private final ListCategories listCategories;

    CategoryController(ListCategories listCategories) {
        this.listCategories = listCategories;
    }

    @GetMapping
    List<CategoryResponse> list() {
        return listCategories.list().stream().map(CategoryResponse::from).toList();
    }
}
