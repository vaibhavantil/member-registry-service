package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class UpdateWebOnBoardingInfoCommand(
    @TargetAggregateIdentifier
    val memberId: Long,
    val SSN: String?,
    val email: String?
)
