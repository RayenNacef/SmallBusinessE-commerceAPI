package com.smallshop.cart.api;

import com.smallshop.cart.application.AddToCart;
import com.smallshop.cart.application.ChangeCartItemQuantity;
import com.smallshop.cart.application.RemoveFromCart;
import com.smallshop.cart.application.ViewCart;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
class CartController {

    private final ViewCart viewCart;
    private final AddToCart addToCart;
    private final ChangeCartItemQuantity changeQuantity;
    private final RemoveFromCart removeFromCart;

    CartController(ViewCart viewCart, AddToCart addToCart, ChangeCartItemQuantity changeQuantity, RemoveFromCart removeFromCart) {
        this.viewCart = viewCart;
        this.addToCart = addToCart;
        this.changeQuantity = changeQuantity;
        this.removeFromCart = removeFromCart;
    }

    @GetMapping
    CartResponse get(@AuthenticationPrincipal Jwt jwt) {
        return CartResponse.from(viewCart.view(customerId(jwt)));
    }

    @PostMapping("/items")
    CartResponse addItem(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddCartItemRequest request) {
        UserId customerId = customerId(jwt);
        addToCart.add(customerId, new ProductId(request.productId()), request.quantity());
        return CartResponse.from(viewCart.view(customerId));
    }

    @PutMapping("/items/{productId}")
    CartResponse changeQuantity(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID productId, @Valid @RequestBody CartQuantityRequest request) {
        UserId customerId = customerId(jwt);
        changeQuantity.change(customerId, new ProductId(productId), request.quantity());
        return CartResponse.from(viewCart.view(customerId));
    }

    @DeleteMapping("/items/{productId}")
    CartResponse removeItem(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID productId) {
        UserId customerId = customerId(jwt);
        removeFromCart.remove(customerId, new ProductId(productId));
        return CartResponse.from(viewCart.view(customerId));
    }

    private static UserId customerId(Jwt jwt) {
        return UserId.fromString(jwt.getSubject());
    }
}
