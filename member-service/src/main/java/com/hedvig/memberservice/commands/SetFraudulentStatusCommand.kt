package com.hedvig.memberservice.commands

import com.hedvig.memberservice.aggregates.FraudulentStatus
import org.axonframework.commandhandling.TargetAggregateIdentifier

class SetFraudulentStatusCommand private constructor(
    @TargetAggregateIdentifier
    val memberId: Long,
    val fraudulentDescription: String,
    val token: String
) {
    var fraudulentStatus = FraudulentStatus.NOT_FRAUD

    constructor(
        memberId: Long,
        fraudulentStatus: String?,
        fraudulentDescription: String,
        token: String
    ) : this(
        memberId,
        fraudulentDescription,
        token
    ) {
        if (fraudulentStatus != null) {
            try {
                this.fraudulentStatus = FraudulentStatus.valueOf(fraudulentStatus)
            } catch (e: IllegalArgumentException) {
                this.fraudulentStatus = FraudulentStatus.NOT_FRAUD
            }
        }
    }
}
