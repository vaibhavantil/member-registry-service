package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

data class NorwegianSignCommand(
    @TargetAggregateIdentifier val id: Long,
    val referenceId: UUID,
    val personalNumber: String,
    val provideJsonResponse: String
)
