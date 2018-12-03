package com.hedvig.memberservice.commands;

import com.hedvig.memberservice.aggregates.FraudulentStatus;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Data
public class SetFraudulentStatusCommand {
  @TargetAggregateIdentifier Long memberId;

  private FraudulentStatus fraudulentStatus = FraudulentStatus.UNDEFINED;
  private String fraudulentDescription;
  private String token;

  public SetFraudulentStatusCommand(Long memberId, String fraudulentStatus, String fraudulentDescription, String token) {
    this (memberId, fraudulentDescription, token);

    if (fraudulentStatus != null) {
      try {
        this.fraudulentStatus = FraudulentStatus.valueOf(fraudulentStatus);
      } catch (IllegalArgumentException e) {
        this.fraudulentStatus = FraudulentStatus.UNDEFINED;
      }

    }
  }

  private SetFraudulentStatusCommand(Long memberId, String fraudulentDescription, String token) {
    this.memberId = memberId;
    this.fraudulentDescription = fraudulentDescription;
    this.token = token;
  }


}
