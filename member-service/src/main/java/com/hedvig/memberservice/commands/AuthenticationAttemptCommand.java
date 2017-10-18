package com.hedvig.memberservice.commands;

import com.hedvig.external.billectaAPI.api.BankIdAuthenticationStatus;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class AuthenticationAttemptCommand {

    @TargetAggregateIdentifier
    private Long id;

    private BankIdAuthenticationStatus bankIdAuthResponse;

}
