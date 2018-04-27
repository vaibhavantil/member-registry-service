package com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto;

import lombok.Value;

@Value
public class BankIdSignRequest {
    private String ssn;
    private String userMessage;
    private String memberId;
}
