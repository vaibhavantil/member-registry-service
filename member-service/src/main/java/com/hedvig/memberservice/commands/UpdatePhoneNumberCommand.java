package com.hedvig.memberservice.commands;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
@AllArgsConstructor
public class UpdatePhoneNumberCommand {

  @TargetAggregateIdentifier
  private long memberId;
  private String phoneNumber;
  private String token;
}
