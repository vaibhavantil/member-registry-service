package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

class InitializeAppleUserCommand(
    @TargetAggregateIdentifier
    val memberId: Long,
    val personalNumber: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val street: String,
    val city: String,
    val zipCode: String
)
