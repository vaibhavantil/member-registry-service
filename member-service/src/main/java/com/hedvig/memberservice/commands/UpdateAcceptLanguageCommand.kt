package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class UpdateAcceptLanguageCommand(
    @TargetAggregateIdentifier val memberId: Long,
    val acceptLanguage: String
)
