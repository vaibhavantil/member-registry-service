package com.hedvig.integration.productsPricing.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Value;

@Value
public class SetCancellationDateRequest {
  UUID insuranceId;
  Instant inactivationDate;
}
