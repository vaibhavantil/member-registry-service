package com.hedvig.integration.notificationService.dto;

import lombok.Value;

@Value
public class CancellationEmailSentToInsurerRequest {
  String insurer;
}
