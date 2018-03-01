package com.hedvig.memberservice.web.dto;

import lombok.Value;

@Value
public class SendWaitIsOverRequest {
    public String name;
    public String waitlistId;
    public String email;
}
