package com.hedvig.memberservice.notificationService.serviceIntegration.productPricingService.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class InsuranceNotificationDTO {
  private UUID insuranceId;
  private String memberId;
  private LocalDateTime activationDate;
}
