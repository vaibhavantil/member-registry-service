package com.hedvig.memberservice.services

import com.hedvig.memberservice.services.dto.StartSwedishBankIdSignResponse

interface UnderwriterSigningService {

    fun startSwedishBankIdSignSession(): StartSwedishBankIdSignResponse
}

