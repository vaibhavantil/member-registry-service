package com.hedvig.memberservice.commands

import com.hedvig.memberservice.web.dto.Market
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class CreateMemberCommand(
    @TargetAggregateIdentifier
    val memberId: Long,
    val acceptLanguage: String? = null
)
