package com.hedvig.memberservice.web.dto;

import lombok.Value;

@Value
public class SendActivatedRequest {
    public String name;
    public String email;
}
