package com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto;

import lombok.Value;

@Value
public class SendOnboardedActiveTodayRequest {
    public String name;
    public String email;
}
