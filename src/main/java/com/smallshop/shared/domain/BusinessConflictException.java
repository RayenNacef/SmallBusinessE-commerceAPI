package com.smallshop.shared.domain;

// Base type so the global exception handler can map every broken business rule to 409 without knowing each module.
public abstract class BusinessConflictException extends RuntimeException {

    protected BusinessConflictException(String message) {
        super(message);
    }
}
