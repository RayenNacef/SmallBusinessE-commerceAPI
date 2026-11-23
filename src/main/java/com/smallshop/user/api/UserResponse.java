package com.smallshop.user.api;

import com.smallshop.user.domain.User;

import java.util.UUID;

record UserResponse(UUID id, String email, String role) {

    static UserResponse from(User user) {
        return new UserResponse(user.id().value(), user.email(), user.role().name());
    }
}
