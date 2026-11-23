package com.smallshop.ordering.api;

import com.smallshop.ordering.application.ManageOrders;
import com.smallshop.ordering.domain.OrderId;
import com.smallshop.ordering.domain.OrderStatus;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
class AdminOrderController {

    private final ManageOrders manageOrders;

    AdminOrderController(ManageOrders manageOrders) {
        this.manageOrders = manageOrders;
    }

    @GetMapping
    PageResult<OrderResponse> list(
            @RequestParam Optional<OrderStatus> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return manageOrders.list(status, new PageQuery(page, size)).map(OrderResponse::from);
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable UUID id) {
        return OrderResponse.from(manageOrders.get(new OrderId(id)));
    }

    // Payment is simulated: the shop owner confirms it by hand.
    @PostMapping("/{id}/pay")
    OrderResponse markPaid(@PathVariable UUID id) {
        return moveTo(id, OrderStatus.PAID);
    }

    @PostMapping("/{id}/ship")
    OrderResponse ship(@PathVariable UUID id) {
        return moveTo(id, OrderStatus.SHIPPED);
    }

    @PostMapping("/{id}/deliver")
    OrderResponse deliver(@PathVariable UUID id) {
        return moveTo(id, OrderStatus.DELIVERED);
    }

    @PostMapping("/{id}/cancel")
    OrderResponse cancel(@PathVariable UUID id) {
        return moveTo(id, OrderStatus.CANCELLED);
    }

    private OrderResponse moveTo(UUID id, OrderStatus target) {
        return OrderResponse.from(manageOrders.moveTo(new OrderId(id), target));
    }
}
