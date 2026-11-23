package com.smallshop.catalog.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {

    @Test
    void nameIsTrimmedOnCreation() {
        Category category = Category.create("  Coffee  ");

        assertThat(category.name()).isEqualTo("Coffee");
    }

    @Test
    void blankNameIsRejected() {
        assertThatThrownBy(() -> Category.create("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tooLongNameIsRejected() {
        String longName = "a".repeat(Category.MAX_NAME_LENGTH + 1);

        assertThatThrownBy(() -> Category.create(longName))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void renamingKeepsTheSameId() {
        Category category = Category.create("Coffee");
        CategoryId originalId = category.id();

        category.rename("Tea");

        assertThat(category.name()).isEqualTo("Tea");
        assertThat(category.id()).isEqualTo(originalId);
    }
}
