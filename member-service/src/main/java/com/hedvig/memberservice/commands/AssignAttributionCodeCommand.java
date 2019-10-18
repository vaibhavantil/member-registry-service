package com.hedvig.memberservice.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class AssignAttributionCodeCommand {
  @TargetAggregateIdentifier long memberId;
  private String attributionCode;
}
