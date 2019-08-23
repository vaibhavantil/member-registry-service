package com.hedvig.integration.notificationService.dto;

import java.time.Instant;
import lombok.Value;

@Value
public class InsuranceActivationDateUpdatedRequest {
  String currentInsurer;
  Instant activationDate;
}
