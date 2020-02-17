package com.hedvig.memberservice.services

import com.hedvig.memberservice.entities.SignStatus
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.member.dto.MemberSignResponse
import com.hedvig.memberservice.services.member.dto.NorwegianBankIdResponse
import com.hedvig.memberservice.web.v2.dto.WebsignRequest
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class NorwegianSigningService(
    private val memberRepository: MemberRepository,
    private val norwegianBankIdService: NorwegianBankIdService
) {

    @Transactional
    fun startSign(memberId: Long, request: WebsignRequest): MemberSignResponse {

        val acceptLanguage = memberRepository.findById(memberId).get().acceptLanguage
        val response = norwegianBankIdService.sign(
            memberId.toString(),
            request.ssn,
            acceptLanguage,
            request.isMobile
        )
        return response.redirectUrl?.let { redirectUrl ->
            MemberSignResponse(
                signUUID = response.id,
                status = SignStatus.IN_PROGRESS,
                norwegianBankIdResponse = NorwegianBankIdResponse(redirectUrl)
            )
        } ?: throw IllegalStateException("Started norwegian sign got no redirect url [Response: $response]")
    }
}
