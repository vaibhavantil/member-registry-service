package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class AuthenticationAttemptCommand(
    @TargetAggregateIdentifier var id: Long,
    val bankIdAuthResponse: BankIdAuthenticationStatus
)
