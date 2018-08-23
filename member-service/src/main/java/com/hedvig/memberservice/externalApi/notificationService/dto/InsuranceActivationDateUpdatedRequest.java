package com.hedvig.memberservice.externalApi.notificationService.dto;

import java.time.Instant;
import lombok.Value;

@Value
public class InsuranceActivationDateUpdatedRequest {
  String currentInsurer;
  Instant activationDate;
}
