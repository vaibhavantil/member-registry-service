package com.hedvig.external.bankID.exceptions;

import bankid.RpFaultType;
import lombok.ToString;

@ToString
public class BankIDError extends RuntimeException {


    public BankIDError( RpFaultType errorMessages) {
        this.errorType = ErrorType.valueOf(errorMessages.getFaultStatus().value());
        this.detail = errorMessages.getDetailedDescription();
    }

    public enum ErrorType {
        INVALID_PARAMETERS,
        ACCESS_DENIED_RP,
        CLIENT_ERR,
        CERTIFICATE_ERR,
        RETRY,
        INTERNAL_ERROR,
        ALREADY_COLLECTED,
        EXPIRED_TRANSACTION,
        ALREADY_IN_PROGRESS,
        USER_CANCEL,
        CANCELLED,
        REQ_PRECOND,
        REQ_ERROR,
        REQ_BLOCKED,
        START_FAILED;
    }

    public ErrorType errorType;
    public String detail;
}
