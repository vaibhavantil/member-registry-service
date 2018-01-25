package com.hedvig.memberservice.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class BankIdSignCommand {
    @TargetAggregateIdentifier
    private Long id;

    private String referenceId;
    private String signature;
    private String oscpResponse;


}
