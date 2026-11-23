package com.smallshop.user.application;

import java.time.Instant;

public record AccessToken(String value, Instant expiresAt) {
}
