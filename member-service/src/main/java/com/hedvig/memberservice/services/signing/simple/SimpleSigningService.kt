package com.hedvig.memberservice.services.signing.simple

import com.hedvig.memberservice.services.signing.simple.dto.SimpleSignStatus
import java.util.UUID

interface SimpleSigningService {
    fun getSignStatus(memberId: Long): SimpleSignStatus?
    fun startSign(memberId: Long, ssn: String): UUID
    fun notifyContractsCreated(memberId: Long)
}

