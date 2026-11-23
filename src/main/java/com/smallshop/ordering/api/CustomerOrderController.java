package com.smallshop.ordering.api;

import com.smallshop.ordering.application.CancelOwnOrder;
import com.smallshop.ordering.application.PlaceOrder;
import com.smallshop.ordering.application.ViewCustomerOrders;
import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderId;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import com.smallshop.shared.domain.UserId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
class CustomerOrderController {

    private final PlaceOrder placeOrder;
    private final ViewCustomerOrders viewOrders;
    private final CancelOwnOrder cancelOwnOrder;

    CustomerOrderController(PlaceOrder placeOrder, ViewCustomerOrders viewOrders, CancelOwnOrder cancelOwnOrder) {
        this.placeOrder = placeOrder;
        this.viewOrders = viewOrders;
        this.cancelOwnOrder = cancelOwnOrder;
    }

    @PostMapping
    ResponseEntity<OrderResponse> placeFromCart(@AuthenticationPrincipal Jwt jwt) {
        Order order = placeOrder.placeFromCart(customerId(jwt));
        URI location = URI.create("/api/orders/" + order.id());
        return ResponseEntity.created(location).body(OrderResponse.from(order));
    }

    @GetMapping
    PageResult<OrderResponse> history(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return viewOrders.history(customerId(jwt), new PageQuery(page, size)).map(OrderResponse::from);
    }

    @GetMapping("/{id}")
    OrderResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return OrderResponse.from(viewOrders.get(customerId(jwt), new OrderId(id)));
    }

    @PostMapping("/{id}/cancel")
    OrderResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return OrderResponse.from(cancelOwnOrder.cancel(customerId(jwt), new OrderId(id)));
    }

    private static UserId customerId(Jwt jwt) {
        return UserId.fromString(jwt.getSubject());
    }
}
