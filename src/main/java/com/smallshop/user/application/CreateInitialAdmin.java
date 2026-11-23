package com.smallshop.user.application;

import com.smallshop.user.domain.Role;
import com.smallshop.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateInitialAdmin {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public CreateInitialAdmin(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public boolean createIfNoAdminExists(String email, String rawPassword) {
        if (users.existsByRole(Role.ADMIN)) {
            return false;
        }
        users.save(User.createAdmin(email, passwordHasher.hash(rawPassword)));
        return true;
    }
}
