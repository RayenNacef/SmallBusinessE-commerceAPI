package com.smallshop.ordering.api;

import com.smallshop.ordering.application.ManageOrders;
import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderLine;
import com.smallshop.ordering.domain.OrderStatus;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
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
import java.util.Optional;

import static com.smallshop.TestTokens.asAdmin;
import static com.smallshop.TestTokens.asCustomer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminOrderController.class)
@Import(SecurityConfiguration.class)
@MockitoBean(types = JwtDecoder.class)
class AdminOrderControllerTest {

    private final Order order = Order.place(UserId.newId(),
            List.of(new OrderLine(ProductId.newId(), "Beans", Money.of("5.00"), 1)),
            Instant.parse("2026-11-23T10:00:00Z"));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageOrders manageOrders;

    @Test
    void adminMarksAnOrderAsPaid() throws Exception {
        order.markPaid();
        when(manageOrders.moveTo(order.id(), OrderStatus.PAID)).thenReturn(order);

        mockMvc.perform(post("/api/admin/orders/{id}/pay", order.id().value()).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void adminFiltersOrdersByStatus() throws Exception {
        when(manageOrders.list(Optional.of(OrderStatus.PENDING), new PageQuery(0, 20)))
                .thenReturn(new PageResult<>(List.of(order), 0, 20, 1, 1));

        mockMvc.perform(get("/api/admin/orders").param("status", "PENDING").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(order.id().toString()));
    }

    @Test
    void unknownStatusFilterIsRejected() throws Exception {
        mockMvc.perform(get("/api/admin/orders").param("status", "LOST").with(asAdmin()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void customerCannotManageOrders() throws Exception {
        mockMvc.perform(post("/api/admin/orders/{id}/ship", order.id().value()).with(asCustomer()))
                .andExpect(status().isForbidden());
    }
}
