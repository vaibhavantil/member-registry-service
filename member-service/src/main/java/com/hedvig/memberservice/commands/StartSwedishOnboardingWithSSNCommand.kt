package com.hedvig.memberservice.commands

import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest
import org.axonframework.commandhandling.TargetAggregateIdentifier

@Deprecated("This is deprecated and will only work for sweden should remove once bot-service is not used for on boarding")
class StartSwedishOnboardingWithSSNCommand(
    @TargetAggregateIdentifier val memberId: Long,
    val requestData: StartOnboardingWithSSNRequest
) {
    val ssn: String
        get() = requestData.ssn
}
