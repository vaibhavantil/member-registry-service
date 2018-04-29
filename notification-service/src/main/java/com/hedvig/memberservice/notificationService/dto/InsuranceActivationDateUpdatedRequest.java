package com.hedvig.memberservice.notificationService.dto;

import lombok.Value;

import java.time.Instant;

@Value
public class InsuranceActivationDateUpdatedRequest {
    String currentInsurer;
    Instant activationDate;
}
