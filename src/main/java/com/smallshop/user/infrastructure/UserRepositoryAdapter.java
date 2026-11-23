package com.smallshop.user.infrastructure;

import com.smallshop.shared.domain.UserId;
import com.smallshop.user.application.UserRepository;
import com.smallshop.user.domain.Role;
import com.smallshop.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    UserRepositoryAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        jpa.save(new UserJpaEntity(user.id().value(), user.email(), user.passwordHash(), user.role()));
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public boolean existsByRole(Role role) {
        return jpa.existsByRole(role);
    }

    private static User toDomain(UserJpaEntity entity) {
        return new User(new UserId(entity.getId()), entity.getEmail(), entity.getPasswordHash(), entity.getRole());
    }
}
