package com.hedvig.memberservice.services.signing.underwriter

import com.hedvig.memberservice.services.signing.simple.dto.StartSimpleSignResponse
import com.hedvig.memberservice.services.signing.sweden.dto.StartSwedishBankIdSignResponse
import com.hedvig.memberservice.services.signing.zignsec.dto.StartZignSecBankIdSignResponse
import java.util.UUID

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
        ssn: String,
        successUrl: String,
        failUrl: String
    ): StartZignSecBankIdSignResponse

    fun startDanishBankIdSignSession(
        underwriterSessionRef: UUID,
        memberId: Long,
        ssn: String,
        successUrl: String,
        failUrl: String
    ): StartZignSecBankIdSignResponse

    fun startSimpleSignSession(underwriterSessionReference: UUID, memberId: Long, ssn: String): StartSimpleSignResponse

    fun isUnderwriterHandlingSignSession(orderReference: UUID): Boolean

    fun swedishBankIdSignSessionWasCompleted(orderReference: String, signature: String, oscpResponse: String)

    fun norwegianBankIdSignSessionWasCompleted(orderReference: UUID)

    fun danishBankIdSignSessionWasCompleted(orderReference: UUID)
}

