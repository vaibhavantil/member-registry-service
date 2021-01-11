package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

@Deprecated("Only used in sweden")
data class UpdateSwedishWebOnBoardingInfoCommand(
    @TargetAggregateIdentifier
    val memberId: Long,
    val ssn: String?,
    val email: String?
)
