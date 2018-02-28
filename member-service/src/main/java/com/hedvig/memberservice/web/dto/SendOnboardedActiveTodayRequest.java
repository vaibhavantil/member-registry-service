package com.hedvig.memberservice.web.dto;

import lombok.Value;

@Value
public class SendOnboardedActiveTodayRequest {
    public String name;
    public String email;
}
