package com.hedvig.memberservice.commands;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import org.joda.time.LocalDateTime;

@Value
public class InsurnaceCancellationCommand {
  @TargetAggregateIdentifier
  Long memberId;

  UUID insuranceId;
  LocalDate inactivationDate;
}
