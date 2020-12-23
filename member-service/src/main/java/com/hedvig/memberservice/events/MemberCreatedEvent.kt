package com.hedvig.memberservice.events

import com.hedvig.memberservice.aggregates.MemberStatus
import java.time.Instant

class MemberCreatedEvent(
    val id: Long,
    val status: MemberStatus,
    val createdOn: Instant = Instant.now()
) {
    constructor(
        id: Long,
        status: MemberStatus
    ) : this(id, status, Instant.now())
}
