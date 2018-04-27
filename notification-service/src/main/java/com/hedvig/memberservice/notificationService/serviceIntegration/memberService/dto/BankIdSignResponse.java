package com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto;

import lombok.Value;

@Value
public class BankIdSignResponse {

    private final String autoStartToken;
    private final String referenceToken;
    private final String status;
}
