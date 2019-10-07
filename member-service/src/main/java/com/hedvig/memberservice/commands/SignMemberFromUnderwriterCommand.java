package com.hedvig.memberservice.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class SignMemberFromUnderwriterCommand {

  @TargetAggregateIdentifier
  private final Long id;
  private final String ssn;
}
