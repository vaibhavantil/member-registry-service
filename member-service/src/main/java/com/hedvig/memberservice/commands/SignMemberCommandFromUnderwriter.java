package com.hedvig.memberservice.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class SignMemberCommandFromUnderwriter {

  @TargetAggregateIdentifier
  private final Long id;

  private final String referenceId;
  private final String signature;
  private final String oscpResponse;
  private final String ssn;

}
