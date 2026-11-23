package com.smallshop.ordering.infrastructure;

import com.smallshop.TestcontainersConfiguration;
import com.smallshop.ordering.domain.Order;
import com.smallshop.ordering.domain.OrderLine;
import com.smallshop.ordering.domain.OrderStatus;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, OrderRepositoryAdapter.class})
class OrderRepositoryAdapterTest {

    @Autowired
    private OrderRepositoryAdapter orders;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TestEntityManager entityManager;

    private UserId alice;
    private UserId bob;
    private ProductId beans;
    private ProductId tea;

    @BeforeEach
    void setUp() {
        alice = insertCustomer();
        bob = insertCustomer();
        UUID category = UUID.randomUUID();
        jdbc.update("insert into categories (id, name) values (?, ?)", category, "Category " + category);
        beans = insertProduct(category);
        tea = insertProduct(category);
    }

    @Test
    void savedOrderKeepsItsLinesInOrderWithFrozenPrices() {
        Order order = Order.place(alice, List.of(
                new OrderLine(tea, "Sencha", Money.of("8.00"), 1),
                new OrderLine(beans, "Espresso", Money.of("12.50"), 2)), Instant.parse("2026-11-23T10:00:00Z"));
        orders.save(order);
        entityManager.flush();
        entityManager.clear();

        Order loaded = orders.findById(order.id()).orElseThrow();

        assertThat(loaded.lines()).containsExactlyElementsOf(order.lines());
        assertThat(loaded.total()).isEqualTo(Money.of("33.00"));
        assertThat(loaded.placedAt()).isEqualTo(order.placedAt());
    }

    @Test
    void statusChangeIsPersistedWithoutTouchingLines() {
        Order order = orders.save(Order.place(alice, List.of(new OrderLine(beans, "Espresso", Money.of("12.50"), 1)), Instant.now()));
        entityManager.flush();
        entityManager.clear();

        Order locked = orders.lockById(order.id()).orElseThrow();
        locked.markPaid();
        orders.save(locked);
        entityManager.flush();
        entityManager.clear();

        Order reloaded = orders.findById(order.id()).orElseThrow();
        assertThat(reloaded.status()).isEqualTo(OrderStatus.PAID);
        assertThat(reloaded.lines()).hasSize(1);
    }

    @Test
    void historyShowsOnlyTheCustomersOrdersNewestFirst() {
        Order older = orders.save(placedBy(alice, "2026-11-01T10:00:00Z"));
        Order newer = orders.save(placedBy(alice, "2026-11-20T10:00:00Z"));
        orders.save(placedBy(bob, "2026-11-21T10:00:00Z"));

        PageResult<Order> history = orders.findByCustomer(alice, new PageQuery(0, 10));

        assertThat(history.items()).extracting(Order::id).containsExactly(newer.id(), older.id());
        assertThat(history.totalItems()).isEqualTo(2);
    }

    @Test
    void adminListingCanFilterByStatus() {
        Order paid = placedBy(alice, "2026-11-01T10:00:00Z");
        paid.markPaid();
        orders.save(paid);
        orders.save(placedBy(bob, "2026-11-02T10:00:00Z"));

        PageResult<Order> paidOrders = orders.findAll(Optional.of(OrderStatus.PAID), new PageQuery(0, 10));

        assertThat(paidOrders.items()).extracting(Order::id).containsExactly(paid.id());
    }

    private Order placedBy(UserId customer, String placedAt) {
        return Order.place(customer, List.of(new OrderLine(beans, "Espresso", Money.of("12.50"), 1)), Instant.parse(placedAt));
    }

    private UserId insertCustomer() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into users (id, email, password_hash, role) values (?, ?, 'hash', 'CUSTOMER')",
                id, id + "@example.com");
        return new UserId(id);
    }

    private ProductId insertProduct(UUID category) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into products (id, name, description, price, category_id, active) values (?, 'P', '', 1.00, ?, true)",
                id, category);
        return new ProductId(id);
    }
}
