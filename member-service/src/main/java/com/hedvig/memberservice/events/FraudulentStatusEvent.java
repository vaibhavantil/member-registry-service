package com.hedvig.memberservice.events;

import com.hedvig.memberservice.aggregates.FraudulentStatus;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class FraudulentStatusEvent {
  private Long memberId;
  private FraudulentStatus fraudulentStatus;
  private String fraudulentDescription;
  private String token;
}
