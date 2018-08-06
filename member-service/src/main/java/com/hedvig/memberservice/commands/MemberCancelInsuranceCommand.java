package com.hedvig.memberservice.commands;

import java.time.LocalDate;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class MemberCancelInsuranceCommand {
  @TargetAggregateIdentifier Long memberId;

  LocalDate inactivationDate;
}
