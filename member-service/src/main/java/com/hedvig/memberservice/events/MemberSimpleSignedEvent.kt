package com.hedvig.memberservice.events

import java.util.UUID

data class MemberSimpleSignedEvent(
    val memberId: Long,
    val nationalIdentification: String,
    val nationality: Nationality,
    val referenceId: UUID
) {
    enum class Nationality {
        SWEDEN,
        NORWAY,
        DENMARK
    }
}
