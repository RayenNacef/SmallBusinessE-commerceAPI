package com.smallshop;

import com.smallshop.cart.application.AddToCart;
import com.smallshop.catalog.application.CreateCategory;
import com.smallshop.catalog.application.CreateProduct;
import com.smallshop.catalog.application.ProductDetails;
import com.smallshop.catalog.domain.CategoryId;
import com.smallshop.inventory.application.ManageStockLevel;
import com.smallshop.inventory.domain.InsufficientStockException;
import com.smallshop.ordering.application.PlaceOrder;
import com.smallshop.shared.domain.Money;
import com.smallshop.shared.domain.ProductId;
import com.smallshop.shared.domain.UserId;
import com.smallshop.user.application.RegisterCustomer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class StockReservationConcurrencyTest {

    private static final int BUYERS = 20;

    @Autowired
    private RegisterCustomer registerCustomer;

    @Autowired
    private CreateCategory createCategory;

    @Autowired
    private CreateProduct createProduct;

    @Autowired
    private AddToCart addToCart;

    @Autowired
    private PlaceOrder placeOrder;

    @Autowired
    private ManageStockLevel manageStockLevel;

    @Test
    void manyBuyersRacingForTheLastUnitsNeverOversell() throws Exception {
        int unitsInStock = 5;
        ProductId product = createProductWithStock(unitsInStock);
        List<UserId> buyers = new ArrayList<>();
        for (int i = 0; i < BUYERS; i++) {
            UserId buyer = registerBuyer();
            addToCart.add(buyer, product, 1);
            buyers.add(buyer);
        }

        List<Outcome> outcomes = placeOrdersAtTheSameTime(buyers);

        assertThat(outcomes).filteredOn(outcome -> outcome == Outcome.PLACED).hasSize(unitsInStock);
        assertThat(outcomes).filteredOn(outcome -> outcome == Outcome.OUT_OF_STOCK).hasSize(BUYERS - unitsInStock);
        assertThat(manageStockLevel.get(product).available()).isZero();
    }

    @Test
    void buyersLockingTheSameProductsInOppositeCartOrderDoNotDeadlock() throws Exception {
        int unitsInStock = 5;
        ProductId beans = createProductWithStock(unitsInStock);
        ProductId tea = createProductWithStock(unitsInStock);
        List<UserId> buyers = new ArrayList<>();
        for (int i = 0; i < BUYERS; i++) {
            UserId buyer = registerBuyer();
            boolean beansFirst = i % 2 == 0;
            addToCart.add(buyer, beansFirst ? beans : tea, 1);
            addToCart.add(buyer, beansFirst ? tea : beans, 1);
            buyers.add(buyer);
        }

        List<Outcome> outcomes = placeOrdersAtTheSameTime(buyers);

        assertThat(outcomes).doesNotContain(Outcome.OTHER_FAILURE);
        assertThat(outcomes).filteredOn(outcome -> outcome == Outcome.PLACED).hasSize(unitsInStock);
        assertThat(manageStockLevel.get(beans).available()).isZero();
        assertThat(manageStockLevel.get(tea).available()).isZero();
    }

    private List<Outcome> placeOrdersAtTheSameTime(List<UserId> buyers) throws Exception {
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Callable<Outcome>> attempts = new ArrayList<>();
        for (UserId buyer : buyers) {
            attempts.add(() -> {
                startSignal.await();
                return tryToPlaceOrder(buyer);
            });
        }

        try (ExecutorService executor = Executors.newFixedThreadPool(buyers.size())) {
            List<Future<Outcome>> futures = new ArrayList<>();
            for (Callable<Outcome> attempt : attempts) {
                futures.add(executor.submit(attempt));
            }
            startSignal.countDown();
            List<Outcome> outcomes = new ArrayList<>();
            for (Future<Outcome> future : futures) {
                outcomes.add(future.get(30, TimeUnit.SECONDS));
            }
            return outcomes;
        }
    }

    private Outcome tryToPlaceOrder(UserId buyer) {
        try {
            placeOrder.placeFromCart(buyer);
            return Outcome.PLACED;
        } catch (InsufficientStockException exception) {
            return Outcome.OUT_OF_STOCK;
        } catch (RuntimeException exception) {
            return Outcome.OTHER_FAILURE;
        }
    }

    private ProductId createProductWithStock(int units) {
        CategoryId category = createCategory.create("Race " + UUID.randomUUID()).id();
        ProductDetails details = new ProductDetails("Last units", "", Money.of("10.00"), category);
        return createProduct.create(details, units).id();
    }

    private UserId registerBuyer() {
        return registerCustomer.register("racer-" + UUID.randomUUID() + "@example.com", "secret-password").id();
    }

    private enum Outcome {
        PLACED,
        OUT_OF_STOCK,
        OTHER_FAILURE
    }
}
