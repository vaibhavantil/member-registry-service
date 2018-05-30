package com.hedvig.memberservice.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.time.Instant;

@Value
public class BankIdSignCommand {
    @TargetAggregateIdentifier
    private Long id;

    private String referenceId;
    private String signature;
    private String oscpResponse;
    private Instant registeredOn;


}
