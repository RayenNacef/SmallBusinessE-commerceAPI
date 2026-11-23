package com.smallshop.user.domain;

import com.smallshop.shared.domain.BusinessConflictException;

public class DuplicateEmailException extends BusinessConflictException {

    public DuplicateEmailException(String email) {
        super("An account with email " + email + " already exists");
    }
}
