package com.smallshop.catalog.domain;

import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;

import java.util.Objects;

public class Product {

    static final int MAX_NAME_LENGTH = 200;
    static final int MAX_DESCRIPTION_LENGTH = 2000;

    private final ProductId id;
    private String name;
    private String description;
    private Money price;
    private CategoryId categoryId;
    private boolean active;

    public Product(ProductId id, String name, String description, Money price, CategoryId categoryId, boolean active) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = validName(name);
        this.description = validDescription(description);
        this.price = validPrice(price);
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId must not be null");
        this.active = active;
    }

    public static Product create(String name, String description, Money price, CategoryId categoryId) {
        return new Product(ProductId.newId(), name, description, price, categoryId, true);
    }

    public void updateDetails(String name, String description, Money price, CategoryId categoryId) {
        this.name = validName(name);
        this.description = validDescription(description);
        this.price = validPrice(price);
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId must not be null");
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public ProductId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Money price() {
        return price;
    }

    public CategoryId categoryId() {
        return categoryId;
    }

    public boolean isActive() {
        return active;
    }

    private static String validName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Product name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        return trimmed;
    }

    private static String validDescription(String description) {
        if (description == null) {
            return "";
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Product description must be at most " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        return description.trim();
    }

    private static Money validPrice(Money price) {
        Objects.requireNonNull(price, "price must not be null");
        if (price.isZero()) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        return price;
    }
}
