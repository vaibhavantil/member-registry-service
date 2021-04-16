package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class PopulateMemberThroughLoginDataCommand(
    @TargetAggregateIdentifier var id: Long,
    val givenName: String?,
    val surname: String?
)
