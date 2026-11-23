package com.smallshop.cart.domain;

import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Cart {

    private final UserId customerId;
    private final Map<ProductId, CartItem> items = new LinkedHashMap<>();

    public Cart(UserId customerId, List<CartItem> items) {
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        for (CartItem item : items) {
            this.items.put(item.productId(), item);
        }
    }

    public static Cart emptyFor(UserId customerId) {
        return new Cart(customerId, List.of());
    }

    public void add(ProductId productId, int quantity) {
        CartItem existing = items.get(productId);
        int newQuantity = existing == null ? quantity : existing.quantity() + quantity;
        items.put(productId, new CartItem(productId, newQuantity));
    }

    public void changeQuantity(ProductId productId, int quantity) {
        if (!items.containsKey(productId)) {
            throw new CartItemNotFoundException(productId);
        }
        items.put(productId, new CartItem(productId, quantity));
    }

    public void remove(ProductId productId) {
        items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    public UserId customerId() {
        return customerId;
    }

    public List<CartItem> items() {
        return List.copyOf(items.values());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
