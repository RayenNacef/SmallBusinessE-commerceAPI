package com.smallshop.user.application;

import com.smallshop.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LogInTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final TokenIssuer tokenIssuer = mock(TokenIssuer.class);
    private final LogIn logIn = new LogIn(users, passwordHasher, tokenIssuer);

    private final User alice = User.registerCustomer("alice@example.com", "hashed");

    @Test
    void correctPasswordReturnsAToken() {
        AccessToken token = new AccessToken("jwt", Instant.parse("2030-01-01T00:00:00Z"));
        when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(passwordHasher.matches("secret-password", "hashed")).thenReturn(true);
        when(tokenIssuer.issueFor(alice)).thenReturn(token);

        assertThat(logIn.logIn("Alice@Example.com", "secret-password")).isEqualTo(token);
    }

    @Test
    void wrongPasswordFails() {
        when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
        when(passwordHasher.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> logIn.logIn("alice@example.com", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void unknownEmailFailsWithTheSameError() {
        when(users.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> logIn.logIn("nobody@example.com", "whatever"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
