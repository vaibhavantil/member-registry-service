package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

class UpdatePhoneNumberCommand(
    @TargetAggregateIdentifier
    val memberId: Long,
    val phoneNumber: String
)
