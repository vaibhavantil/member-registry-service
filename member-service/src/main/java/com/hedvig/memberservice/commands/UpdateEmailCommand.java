package com.hedvig.memberservice.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class UpdateEmailCommand {
  @TargetAggregateIdentifier Long id;
  String email;
}
