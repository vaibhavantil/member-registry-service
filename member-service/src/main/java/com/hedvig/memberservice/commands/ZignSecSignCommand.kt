package com.hedvig.memberservice.commands

import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

data class ZignSecSignCommand(
    @TargetAggregateIdentifier val id: Long,
    val referenceId: UUID,
    val personalNumber: String,
    val provideJsonResponse: String,
    val zignSecAuthMarket: ZignSecAuthenticationMarket
)
