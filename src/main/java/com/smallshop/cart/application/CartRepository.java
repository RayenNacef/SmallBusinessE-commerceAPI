package com.smallshop.cart.application;

import com.smallshop.cart.domain.Cart;
import com.smallshop.shared.domain.UserId;

import java.util.Optional;

public interface CartRepository {

    Optional<Cart> findByCustomer(UserId customerId);

    Cart save(Cart cart);
}
