package com.smallshop.catalog.domain;

import java.util.Objects;

public class Category {

    static final int MAX_NAME_LENGTH = 100;

    private final CategoryId id;
    private String name;

    public Category(CategoryId id, String name) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = validName(name);
    }

    public static Category create(String name) {
        return new Category(CategoryId.newId(), name);
    }

    public void rename(String newName) {
        this.name = validName(newName);
    }

    public CategoryId id() {
        return id;
    }

    public String name() {
        return name;
    }

    private static String validName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name must not be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Category name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        return trimmed;
    }
}
