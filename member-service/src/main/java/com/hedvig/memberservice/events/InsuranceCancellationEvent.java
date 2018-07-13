package com.hedvig.memberservice.events;

import java.time.Instant;
import java.util.UUID;
import lombok.Value;

@Value
public class InsuranceCancellationEvent {
  Long memberId;
  UUID insuranceId;
  Instant inactivationDate;
}
