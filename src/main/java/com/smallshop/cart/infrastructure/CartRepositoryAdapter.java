package com.smallshop.cart.infrastructure;

import com.smallshop.cart.application.CartRepository;
import com.smallshop.cart.domain.Cart;
import com.smallshop.cart.domain.CartItem;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class CartRepositoryAdapter implements CartRepository {

    private final CartJpaRepository jpa;

    CartRepositoryAdapter(CartJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Cart> findByCustomer(UserId customerId) {
        return jpa.findById(customerId.value()).map(CartRepositoryAdapter::toDomain);
    }

    @Override
    public Cart save(Cart cart) {
        List<CartItemEmbeddable> items = cart.items().stream()
                .map(item -> new CartItemEmbeddable(item.productId().value(), item.quantity()))
                .toList();
        jpa.save(new CartJpaEntity(cart.customerId().value(), items));
        return cart;
    }

    private static Cart toDomain(CartJpaEntity entity) {
        List<CartItem> items = entity.getItems().stream()
                .map(item -> new CartItem(new ProductId(item.getProductId()), item.getQuantity()))
                .toList();
        return new Cart(new UserId(entity.getCustomerId()), items);
    }
}
