package com.smallshop.user.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties("app.jwt")
record JwtProperties(String secret, Duration timeToLive) {

    // HS256 needs a key of at least 256 bits; a shorter secret would make tokens easy to forge.
    private static final int MIN_SECRET_BYTES = 32;

    JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("app.jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes");
        }
        if (timeToLive == null) {
            timeToLive = Duration.ofHours(1);
        }
    }
}
