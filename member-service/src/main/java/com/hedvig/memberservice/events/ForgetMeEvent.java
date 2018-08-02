package com.hedvig.memberservice.events;

import lombok.Value;
import org.axonframework.commandhandling.model.AggregateIdentifier;

@Value
public class ForgetMeEvent {

  @AggregateIdentifier
  public String memberId;
}