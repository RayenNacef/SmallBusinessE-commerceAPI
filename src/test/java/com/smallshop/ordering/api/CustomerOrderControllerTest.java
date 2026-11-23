package com.smallshop.ordering.api;

import com.smallshop.ordering.application.CancelOwnOrder;
import com.smallshop.ordering.application.EmptyCartException;
import com.smallshop.ordering.application.PlaceOrder;
import com.smallshop.ordering.application.ViewCustomerOrders;
import com.smallshop.ordering.domain.InvalidOrderTransitionException;
import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderId;
import com.smallshop.ordering.domain.OrderLine;
import com.smallshop.ordering.domain.OrderNotFoundException;
import com.smallshop.ordering.domain.OrderStatus;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import com.smallshop.user.infrastructure.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.smallshop.TestTokens.asAdmin;
import static com.smallshop.TestTokens.asCustomer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerOrderController.class)
@Import(SecurityConfiguration.class)
@MockitoBean(types = JwtDecoder.class)
class CustomerOrderControllerTest {

    private final UserId customer = UserId.newId();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceOrder placeOrder;

    @MockitoBean
    private ViewCustomerOrders viewOrders;

    @MockitoBean
    private CancelOwnOrder cancelOwnOrder;

    @Test
    void placingAnOrderReturnsItWithFrozenPricesAndTotal() throws Exception {
        Order order = Order.place(customer,
                List.of(new OrderLine(ProductId.newId(), "Beans", Money.of("12.50"), 2)),
                Instant.parse("2026-11-23T10:00:00Z"));
        when(placeOrder.placeFromCart(customer)).thenReturn(order);

        mockMvc.perform(post("/api/orders").with(asCustomer(customer)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/orders/" + order.id()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.lines[0].unitPrice").value(12.50))
                .andExpect(jsonPath("$.total").value(25.00));
    }

    @Test
    void placingAnOrderFromAnEmptyCartIsAConflict() throws Exception {
        when(placeOrder.placeFromCart(customer)).thenThrow(new EmptyCartException());

        mockMvc.perform(post("/api/orders").with(asCustomer(customer)))
                .andExpect(status().isConflict());
    }

    @Test
    void cancellingAShippedOrderIsAConflict() throws Exception {
        OrderId orderId = OrderId.newId();
        when(cancelOwnOrder.cancel(customer, orderId))
                .thenThrow(new InvalidOrderTransitionException(orderId, OrderStatus.SHIPPED, OrderStatus.CANCELLED));

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId.value()).with(asCustomer(customer)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Order " + orderId + " cannot move from SHIPPED to CANCELLED"));
    }

    @Test
    void anotherCustomersOrderIsNotFound() throws Exception {
        OrderId orderId = new OrderId(UUID.randomUUID());
        when(viewOrders.get(customer, orderId)).thenThrow(new OrderNotFoundException(orderId));

        mockMvc.perform(get("/api/orders/{id}", orderId.value()).with(asCustomer(customer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCannotPlaceCustomerOrders() throws Exception {
        mockMvc.perform(post("/api/orders").with(asAdmin()))
                .andExpect(status().isForbidden());
    }
}
