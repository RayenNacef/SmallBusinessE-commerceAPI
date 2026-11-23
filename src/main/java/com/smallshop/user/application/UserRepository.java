package com.smallshop.user.application;

import com.smallshop.user.domain.Role;
import com.smallshop.user.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);
}
