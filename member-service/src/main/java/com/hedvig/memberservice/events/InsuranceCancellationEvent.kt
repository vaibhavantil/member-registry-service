package com.hedvig.memberservice.events

import java.time.Instant
import java.util.UUID

class InsuranceCancellationEvent(
    var memberId: Long?,
    var insuranceId: UUID?,
    var inactivationDate: Instant?
)
