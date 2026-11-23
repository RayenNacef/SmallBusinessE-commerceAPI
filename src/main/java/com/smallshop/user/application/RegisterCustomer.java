package com.smallshop.user.application;

import com.smallshop.user.domain.DuplicateEmailException;
import com.smallshop.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterCustomer {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public RegisterCustomer(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public User register(String email, String rawPassword) {
        String normalizedEmail = User.normalizeEmail(email);
        if (users.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }
        return users.save(User.registerCustomer(normalizedEmail, passwordHasher.hash(rawPassword)));
    }
}
