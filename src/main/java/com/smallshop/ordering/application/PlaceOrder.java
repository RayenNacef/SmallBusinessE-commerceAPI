package com.smallshop.ordering.application;

import com.smallshop.cart.application.CartCheckout;
import com.smallshop.cart.application.CartLine;
import com.smallshop.catalog.application.ProductLookup;
import com.smallshop.catalog.application.ProductSummary;
import com.smallshop.inventory.application.StockReservation;
import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderLine;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PlaceOrder {

    private final CartCheckout cartCheckout;
    private final ProductLookup productLookup;
    private final StockReservation stockReservation;
    private final OrderRepository orders;
    private final Clock clock;

    public PlaceOrder(
            CartCheckout cartCheckout,
            ProductLookup productLookup,
            StockReservation stockReservation,
            OrderRepository orders,
            Clock clock) {
        this.cartCheckout = cartCheckout;
        this.productLookup = productLookup;
        this.stockReservation = stockReservation;
        this.orders = orders;
        this.clock = clock;
    }

    // 1 transaction si the stock reservation fails, no order is saved and the cart is left untouched.
    @Transactional
    public Order placeFromCart(UserId customerId) {
        List<CartLine> cartLines = cartCheckout.linesOf(customerId);
        if (cartLines.isEmpty()) {
            throw new EmptyCartException();
        }
        Order order = Order.place(customerId, priceLines(cartLines), clock.instant());
        stockReservation.reserve(order.quantitiesByProduct());
        orders.save(order);
        cartCheckout.clear(customerId);
        return order;
    }

    private List<OrderLine> priceLines(List<CartLine> cartLines) {
        List<ProductId> productIds = cartLines.stream().map(CartLine::productId).toList();
        Map<ProductId, ProductSummary> products = productLookup.findAll(productIds);
        List<OrderLine> lines = new ArrayList<>();
        for (CartLine cartLine : cartLines) {
            ProductSummary product = products.get(cartLine.productId());
            if (product == null || !product.active()) {
                throw new ProductUnavailableException(cartLine.productId());
            }
            lines.add(new OrderLine(product.id(), product.name(), product.price(), cartLine.quantity()));
        }
        return lines;
    }
}
