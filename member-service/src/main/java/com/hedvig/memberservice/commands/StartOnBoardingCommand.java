package com.hedvig.memberservice.commands;

import com.hedvig.external.billectaAPI.api.BankIdAuthenticationStatus;
import com.hedvig.external.bisnodeBCI.dto.Person;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class StartOnBoardingCommand {

    @TargetAggregateIdentifier
    private final Long id;
    private final BankIdAuthenticationStatus bankIdStatus;
    private final Person person;

}
