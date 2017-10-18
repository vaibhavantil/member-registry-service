package com.hedvig.memberservice.aggregates.exceptions;

public class BankIdReferenceUsedException extends RuntimeException {
    public BankIdReferenceUsedException(String message) {
        super(message);
    }
}
