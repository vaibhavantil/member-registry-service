package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class PopulateMemberFromLoginDataCommand(
    @TargetAggregateIdentifier var id: Long,
    val givenName: String?,
    val surname: String?
)
