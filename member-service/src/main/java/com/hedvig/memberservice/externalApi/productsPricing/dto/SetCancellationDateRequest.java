package com.hedvig.memberservice.externalApi.productsPricing.dto;

import java.util.UUID;
import lombok.Value;

import java.time.Instant;

@Value
public class SetCancellationDateRequest {
    UUID insuranceId;
    Instant inactivationDate;
}
