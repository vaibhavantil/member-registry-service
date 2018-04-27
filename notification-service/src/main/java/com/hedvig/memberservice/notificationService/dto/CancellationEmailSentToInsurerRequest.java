package com.hedvig.memberservice.notificationService.dto;

import lombok.Value;

@Value
public class CancellationEmailSentToInsurerRequest {
    String insurer;
}
