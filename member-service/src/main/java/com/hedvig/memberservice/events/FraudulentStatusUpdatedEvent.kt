package com.hedvig.memberservice.events

import com.hedvig.memberservice.aggregates.FraudulentStatus
import java.util.HashMap

class FraudulentStatusUpdatedEvent(
    override val memberId: Long,
    val fraudulentStatus: FraudulentStatus? = null,
    val fraudulentDescription: String? = null
) : Traceable {
    override fun getValues() = mapOf(
        "Fraudulent status" to fraudulentStatus,
        "Fraudulent description" to fraudulentDescription
    )
}
