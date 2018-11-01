package com.hedvig.memberservice.commands;

import java.util.UUID;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class AssignTrackingIdCommand {
  @TargetAggregateIdentifier Long memberid;
  UUID trackingId;
}
