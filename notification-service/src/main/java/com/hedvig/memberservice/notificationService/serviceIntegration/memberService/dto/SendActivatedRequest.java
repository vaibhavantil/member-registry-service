package com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto;

import lombok.Value;

@Value
public class SendActivatedRequest {
    public String name;
    public String email;
}

