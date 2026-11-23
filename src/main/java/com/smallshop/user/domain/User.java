package com.smallshop.user.domain;

import com.smallshop.shared.domain.UserId;

import java.util.Locale;
import java.util.Objects;

public class User {

    private final UserId id;
    private final String email;
    private final String passwordHash;
    private final Role role;

    public User(UserId id, String email, String passwordHash, Role role) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.email = normalizeEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        this.role = Objects.requireNonNull(role, "role must not be null");
    }

    public static User registerCustomer(String email, String passwordHash) {
        return new User(UserId.newId(), email, passwordHash, Role.CUSTOMER);
    }

    public static User createAdmin(String email, String passwordHash) {
        return new User(UserId.newId(), email, passwordHash, Role.ADMIN);
    }

    // Emails are compared case-insensitively, so they are stored in one canonical form.
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public UserId id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }
}
