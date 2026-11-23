package com.smallshop.catalog.domain;

import com.smallshop.shared.domain.BusinessConflictException;

public class DuplicateCategoryNameException extends BusinessConflictException {

    public DuplicateCategoryNameException(String name) {
        super("A category named '" + name + "' already exists");
    }
}
