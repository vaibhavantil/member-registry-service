package com.hedvig.memberservice.events

import com.hedvig.memberservice.aggregates.MemberStatus

class MemberStartedOnBoardingEvent(
    val memberId: Long,
    val newStatus: MemberStatus
)
