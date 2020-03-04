package com.hedvig.memberservice.services

import com.hedvig.memberservice.services.dto.StartSwedishBankIdSignResponse
import org.springframework.stereotype.Service
import java.util.*

@Service
class UnderwriterSigningServiceImpl(): UnderwriterSigningService {
    override fun startSwedishBankIdSignSession(): StartSwedishBankIdSignResponse {
        return StartSwedishBankIdSignResponse("")
    }
}
