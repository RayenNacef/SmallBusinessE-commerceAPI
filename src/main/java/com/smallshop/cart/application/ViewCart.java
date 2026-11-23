package com.smallshop.cart.application;

import com.smallshop.cart.domain.Cart;
import com.smallshop.cart.domain.CartItem;
import com.smallshop.catalog.application.ProductLookup;
import com.smallshop.catalog.application.ProductSummary;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ViewCart {

    private final CartRepository carts;
    private final ProductLookup productLookup;

    public ViewCart(CartRepository carts, ProductLookup productLookup) {
        this.carts = carts;
        this.productLookup = productLookup;
    }

    //prices come from the catalog each time soothe cart shows current prices, only an order freezes them
    @Transactional(readOnly = true)
    public CartView view(UserId customerId) {
        Cart cart = carts.findByCustomer(customerId).orElseGet(() -> Cart.emptyFor(customerId));
        List<ProductId> productIds = cart.items().stream().map(CartItem::productId).toList();
        Map<ProductId, ProductSummary> products = productLookup.findAll(productIds);

        List<CartView.Line> lines = new ArrayList<>();
        Money total = Money.ZERO;
        for (CartItem item : cart.items()) {
            ProductSummary product = products.get(item.productId());
            Money lineTotal = product.price().multiply(item.quantity());
            lines.add(new CartView.Line(
                    item.productId(), product.name(), product.price(), item.quantity(), lineTotal, product.active()));
            if (product.active()) {
                total = total.add(lineTotal);
            }
        }
        return new CartView(lines, total);
    }
}
