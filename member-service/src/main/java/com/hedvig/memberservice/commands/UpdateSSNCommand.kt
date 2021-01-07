package com.hedvig.memberservice.commands

import com.hedvig.memberservice.web.dto.Nationality
import org.axonframework.commandhandling.TargetAggregateIdentifier

class UpdateSSNCommand(
    @TargetAggregateIdentifier
    val memberId: Long,
    val ssn: String,
    val nationality: Nationality
)
