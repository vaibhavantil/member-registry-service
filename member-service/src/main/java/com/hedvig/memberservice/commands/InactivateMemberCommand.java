package com.hedvig.memberservice.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class InactivateMemberCommand {

    @TargetAggregateIdentifier
    private final Long id;

    public InactivateMemberCommand(Long id) {

        this.id = id;
    }
}
