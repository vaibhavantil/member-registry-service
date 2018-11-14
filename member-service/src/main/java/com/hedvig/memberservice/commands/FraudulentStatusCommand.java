package com.hedvig.memberservice.commands;

import com.hedvig.memberservice.aggregates.FraudulentStatus;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.time.LocalDate;
import java.util.UUID;

@Value
public class FraudulentStatusCommand {
  @TargetAggregateIdentifier Long memberId;

  private FraudulentStatus fraudulentStatus;
  private String fraudulentDescription;
  private String token;

}
