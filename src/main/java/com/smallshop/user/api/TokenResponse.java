package com.smallshop.user.api;

import com.smallshop.user.application.AccessToken;

import java.time.Instant;

record TokenResponse(String accessToken, String tokenType, Instant expiresAt) {

    static TokenResponse from(AccessToken token) {
        return new TokenResponse(token.value(), "Bearer", token.expiresAt());
    }
}
