package com.smallshop.shared.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void amountIsNormalisedToTwoDecimals() {
        assertThat(Money.of("10").amount()).isEqualTo(new BigDecimal("10.00"));
        assertThat(Money.of("10.5")).isEqualTo(Money.of("10.50"));
    }

    @Test
    void negativeAmountIsRejected() {
        assertThatThrownBy(() -> Money.of("-0.01"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void amountWithMoreThanTwoDecimalsIsRejected() {
        assertThatThrownBy(() -> Money.of("1.999"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void trailingZerosBeyondTwoDecimalsAreAccepted() {
        assertThat(Money.of("1.5000")).isEqualTo(Money.of("1.50"));
    }

    @Test
    void addingSumsAmounts() {
        assertThat(Money.of("0.10").add(Money.of("0.20"))).isEqualTo(Money.of("0.30"));
    }

    @Test
    void multiplyingByQuantityGivesLineTotal() {
        assertThat(Money.of("19.99").multiply(3)).isEqualTo(Money.of("59.97"));
    }

    @Test
    void multiplyingByNegativeQuantityIsRejected() {
        assertThatThrownBy(() -> Money.of("1.00").multiply(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
