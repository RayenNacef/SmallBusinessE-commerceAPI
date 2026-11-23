package com.smallshop.catalog.infrastructure;

import com.smallshop.TestcontainersConfiguration;
import com.smallshop.catalog.application.ProductSearchCriteria;
import com.smallshop.catalog.domain.Category;
import com.smallshop.catalog.domain.CategoryId;
import com.smallshop.catalog.domain.Product;
import com.smallshop.shared.application.PageQuery;
import com.smallshop.shared.application.PageResult;
import com.smallshop.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, ProductRepositoryAdapter.class, CategoryRepositoryAdapter.class})
class ProductRepositoryAdapterTest {

    @Autowired
    private ProductRepositoryAdapter products;

    @Autowired
    private CategoryRepositoryAdapter categories;

    private CategoryId coffee;
    private CategoryId tea;

    @BeforeEach
    void setUp() {
        coffee = categories.save(Category.create("Coffee")).id();
        tea = categories.save(Category.create("Tea")).id();
    }

    @Test
    void savedProductCanBeLoadedWithSamePrice() {
        Product product = products.save(Product.create("Espresso", "Dark", Money.of("12.50"), coffee));

        Product loaded = products.findById(product.id()).orElseThrow();

        assertThat(loaded.name()).isEqualTo("Espresso");
        assertThat(loaded.price()).isEqualTo(Money.of("12.50"));
        assertThat(loaded.categoryId()).isEqualTo(coffee);
    }

    @Test
    void activeOnlySearchHidesDeactivatedProducts() {
        products.save(Product.create("Espresso", "", Money.of("12.50"), coffee));
        Product discontinued = Product.create("Old blend", "", Money.of("9.00"), coffee);
        discontinued.deactivate();
        products.save(discontinued);

        PageResult<Product> page = products.search(criteria(Optional.empty(), Optional.empty(), true), new PageQuery(0, 10));

        assertThat(page.items()).extracting(Product::name).containsExactly("Espresso");
    }

    @Test
    void searchCanFilterByCategory() {
        products.save(Product.create("Espresso", "", Money.of("12.50"), coffee));
        products.save(Product.create("Sencha", "", Money.of("8.00"), tea));

        PageResult<Product> page = products.search(criteria(Optional.of(tea), Optional.empty(), true), new PageQuery(0, 10));

        assertThat(page.items()).extracting(Product::name).containsExactly("Sencha");
    }

    @Test
    void nameSearchIsCaseInsensitiveAndMatchesPartOfTheName() {
        products.save(Product.create("Espresso Roast", "", Money.of("12.50"), coffee));
        products.save(Product.create("Sencha", "", Money.of("8.00"), tea));

        PageResult<Product> page = products.search(criteria(Optional.empty(), Optional.of("ROAST"), true), new PageQuery(0, 10));

        assertThat(page.items()).extracting(Product::name).containsExactly("Espresso Roast");
    }

    @Test
    void wildcardCharactersInSearchAreMatchedLiterally() {
        products.save(Product.create("100% Arabica", "", Money.of("12.50"), coffee));
        products.save(Product.create("1000 grams of beans", "", Money.of("20.00"), coffee));

        PageResult<Product> page = products.search(criteria(Optional.empty(), Optional.of("100%"), true), new PageQuery(0, 10));

        assertThat(page.items()).extracting(Product::name).containsExactly("100% Arabica");
    }

    @Test
    void resultsArePaginatedInNameOrder() {
        products.save(Product.create("C beans", "", Money.of("1.00"), coffee));
        products.save(Product.create("A beans", "", Money.of("1.00"), coffee));
        products.save(Product.create("B beans", "", Money.of("1.00"), coffee));

        PageResult<Product> secondPage = products.search(criteria(Optional.of(coffee), Optional.empty(), true), new PageQuery(1, 2));

        assertThat(secondPage.items()).extracting(Product::name).containsExactly("C beans");
        assertThat(secondPage.totalItems()).isEqualTo(3);
        assertThat(secondPage.totalPages()).isEqualTo(2);
        assertThat(secondPage.page()).isEqualTo(1);
    }

    private static ProductSearchCriteria criteria(Optional<CategoryId> categoryId, Optional<String> search, boolean activeOnly) {
        return new ProductSearchCriteria(categoryId, search, activeOnly);
    }
}
