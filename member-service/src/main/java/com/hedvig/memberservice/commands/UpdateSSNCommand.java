package com.hedvig.memberservice.commands;

import com.hedvig.memberservice.web.dto.Nationality;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class UpdateSSNCommand {
  @TargetAggregateIdentifier long memberId;
    private String SSN;
    private Nationality nationality;
}
