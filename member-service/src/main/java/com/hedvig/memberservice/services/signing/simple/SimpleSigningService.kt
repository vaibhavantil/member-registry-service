package com.hedvig.memberservice.services.signing.simple

import com.hedvig.memberservice.services.signing.simple.dto.SimpleSignStatus
import com.hedvig.memberservice.web.dto.NationalIdentification
import com.hedvig.memberservice.web.dto.Nationality
import java.util.UUID

interface SimpleSigningService {
    fun getSignStatus(memberId: Long): SimpleSignStatus?
    fun startSign(sessionId: UUID, memberId: Long, nationalIdentification: NationalIdentification)
    fun notifyContractsCreated(memberId: Long)
}

