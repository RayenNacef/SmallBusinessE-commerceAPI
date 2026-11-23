package com.smallshop.shared.domain;

// Base type so the global exception handler can map every "not found" to 404 without knowing each module.
public abstract class ResourceNotFoundException extends RuntimeException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }
}
