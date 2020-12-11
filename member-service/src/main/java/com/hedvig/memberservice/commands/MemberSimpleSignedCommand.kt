package com.hedvig.memberservice.commands

import com.hedvig.memberservice.web.dto.NationalIdentification
import com.hedvig.memberservice.web.dto.Nationality
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

data class MemberSimpleSignedCommand(
    @TargetAggregateIdentifier
    val id: Long,
    val nationalIdentification: NationalIdentification,
    val referenceId: UUID
)
