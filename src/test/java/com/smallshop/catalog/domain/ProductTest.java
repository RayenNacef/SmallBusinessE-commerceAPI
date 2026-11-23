package com.smallshop.catalog.domain;

import com.smallshop.shared.domain.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private final CategoryId coffee = CategoryId.newId();

    @Test
    void newProductIsActive() {
        Product product = Product.create("Espresso beans", "Dark roast", Money.of("12.50"), coffee);

        assertThat(product.isActive()).isTrue();
        assertThat(product.id()).isNotNull();
    }

    @Test
    void missingDescriptionBecomesEmpty() {
        Product product = Product.create("Espresso beans", null, Money.of("12.50"), coffee);

        assertThat(product.description()).isEmpty();
    }

    @Test
    void zeroPriceIsRejected() {
        assertThatThrownBy(() -> Product.create("Free sample", "", Money.ZERO, coffee))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankNameIsRejected() {
        assertThatThrownBy(() -> Product.create(" ", "", Money.of("1.00"), coffee))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tooLongDescriptionIsRejected() {
        String description = "a".repeat(Product.MAX_DESCRIPTION_LENGTH + 1);

        assertThatThrownBy(() -> Product.create("Beans", description, Money.of("1.00"), coffee))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updatingDetailsChangesNamePriceAndCategory() {
        Product product = Product.create("Espresso beans", "", Money.of("12.50"), coffee);
        CategoryId tea = CategoryId.newId();

        product.updateDetails("Green tea", "Sencha", Money.of("8.00"), tea);

        assertThat(product.name()).isEqualTo("Green tea");
        assertThat(product.description()).isEqualTo("Sencha");
        assertThat(product.price()).isEqualTo(Money.of("8.00"));
        assertThat(product.categoryId()).isEqualTo(tea);
    }

    @Test
    void deactivatedProductCanBeReactivated() {
        Product product = Product.create("Espresso beans", "", Money.of("12.50"), coffee);

        product.deactivate();
        assertThat(product.isActive()).isFalse();

        product.activate();
        assertThat(product.isActive()).isTrue();
    }
}
