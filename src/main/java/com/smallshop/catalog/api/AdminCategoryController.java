package com.smallshop.catalog.api;

import com.smallshop.catalog.application.CreateCategory;
import com.smallshop.catalog.application.RenameCategory;
import com.smallshop.catalog.domain.Category;
import com.smallshop.catalog.domain.CategoryId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/categories")
class AdminCategoryController {

    private final CreateCategory createCategory;
    private final RenameCategory renameCategory;

    AdminCategoryController(CreateCategory createCategory, RenameCategory renameCategory) {
        this.createCategory = createCategory;
        this.renameCategory = renameCategory;
    }

    @PostMapping
    ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        Category category = createCategory.create(request.name());
        URI location = URI.create("/api/categories/" + category.id());
        return ResponseEntity.created(location).body(CategoryResponse.from(category));
    }

    @PutMapping("/{id}")
    CategoryResponse rename(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        return CategoryResponse.from(renameCategory.rename(new CategoryId(id), request.name()));
    }
}
