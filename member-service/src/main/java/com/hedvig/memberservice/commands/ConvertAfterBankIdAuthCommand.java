package com.hedvig.memberservice.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.time.LocalDate;

@Value
public class ConvertAfterBankIdAuthCommand {
    @TargetAggregateIdentifier
    private Long id;
    private String personalIdentificationNumber;
    private String givenName;
    private String surname;
    private String name;
}
