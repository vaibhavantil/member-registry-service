package com.hedvig.memberservice.events

import java.time.Instant
import java.util.UUID

class InsuranceCancellationEvent(
    val memberId: Long,
    val insuranceId: UUID?,
    val inactivationDate: Instant?
)
