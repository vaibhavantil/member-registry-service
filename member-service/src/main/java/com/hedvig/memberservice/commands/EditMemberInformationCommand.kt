package com.hedvig.memberservice.commands

import com.hedvig.memberservice.web.dto.InternalMember
import org.axonframework.commandhandling.TargetAggregateIdentifier

class EditMemberInformationCommand(
    @TargetAggregateIdentifier
    val id: String,
    val member: InternalMember,
    val token: String
)
