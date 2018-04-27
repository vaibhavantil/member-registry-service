package com.hedvig.memberservice.notificationService.serviceIntegration.expo;

import lombok.Value;

@Value
class ExpoPushDTO {
    private String to;
    private String title;
    private String body;
}