package com.smallshop.user.application;

import com.smallshop.user.domain.DuplicateEmailException;
import com.smallshop.user.domain.Role;
import com.smallshop.user.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterCustomerTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final RegisterCustomer registerCustomer = new RegisterCustomer(users, passwordHasher);

    @Test
    void registeredUserIsACustomerWithHashedPasswordAndNormalizedEmail() {
        when(passwordHasher.hash("secret-password")).thenReturn("hashed");
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User user = registerCustomer.register("  Alice@Example.COM ", "secret-password");

        assertThat(user.email()).isEqualTo("alice@example.com");
        assertThat(user.passwordHash()).isEqualTo("hashed");
        assertThat(user.role()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void registeringAnEmailThatIsAlreadyTakenFails() {
        when(users.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registerCustomer.register("Alice@example.com", "secret-password"))
                .isInstanceOf(DuplicateEmailException.class);
        verify(users, never()).save(any());
    }
}
