package com.hedvig.memberservice.notificationService.serviceIntegration.memberService.exceptions;

public enum ErrorType {
    INVALID_PARAMETERS,
    ACCESS_DENIED_RP,
    CLIENT_ERR,
    CERTIFICATE_ERR,
    RETRY,
    INTERNAL_ERROR,
    EXPIRED_TRANSACTION,
    ALREADY_IN_PROGRESS,
    USER_CANCEL,
    CANCELLED,
    START_FAILED;
}
