package com.hedvig.memberservice.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class UpdateWebOnBoardingInfoCommand {

  @TargetAggregateIdentifier
  private Long memberId;
  private String SSN;
  private String email;
}
