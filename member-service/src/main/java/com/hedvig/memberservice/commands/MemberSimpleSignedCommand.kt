package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

data class MemberSimpleSignedCommand(
    @TargetAggregateIdentifier
    val id: Long,
    val ssn: String,
    val referenceId: UUID
)
