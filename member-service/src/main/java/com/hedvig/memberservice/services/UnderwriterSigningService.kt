package com.hedvig.memberservice.services

import com.hedvig.memberservice.services.dto.StartNorwegianBankIdSignResponse
import com.hedvig.memberservice.services.dto.StartSwedishBankIdSignResponse
import java.util.*

interface UnderwriterSigningService {

    fun startSwedishBankIdSignSession(
        underwriterSessionRef: UUID,
        memberId: Long,
        ssn: String,
        ipAddress: String,
        isSwitching: Boolean
    ): StartSwedishBankIdSignResponse

    fun startNorwegianBankIdSignSession(
        underwriterSessionRef: UUID,
        memberId: Long,
        ssn: String
    ): StartNorwegianBankIdSignResponse
}

