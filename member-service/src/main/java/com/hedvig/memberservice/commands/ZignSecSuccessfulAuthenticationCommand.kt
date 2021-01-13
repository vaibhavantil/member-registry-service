package com.hedvig.memberservice.commands

import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

data class ZignSecSuccessfulAuthenticationCommand(
    @TargetAggregateIdentifier val id: Long,
    val referenceId: UUID,
    val personalNumber: String,
    val zignSecAuthMarket: ZignSecAuthenticationMarket,
    val firstName: String?,
    val lastName: String?
)
