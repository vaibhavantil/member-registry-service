package com.hedvig.memberservice.events

import com.hedvig.memberservice.aggregates.BisnodeInformation

class PersonInformationFromBisnodeEvent(
    val memberId: Long,
    val information: BisnodeInformation
)
