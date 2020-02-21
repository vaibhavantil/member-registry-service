package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.NorwegianSignResult
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.external.event.NorwegianSignEvent
import com.hedvig.external.zignSec.ZignSecServiceImpl
import com.hedvig.memberservice.entities.SignStatus
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.member.MemberService
import com.hedvig.memberservice.services.member.dto.MemberSignResponse
import com.hedvig.memberservice.services.member.dto.NorwegianBankIdResponse
import com.hedvig.memberservice.web.v2.dto.WebsignRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class NorwegianSigningService(
    private val memberRepository: MemberRepository,
    private val memberService: MemberService,
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

        return when (response) {
            is StartNorwegianAuthenticationResult.Success -> MemberSignResponse(
                signUUID = response.id,
                status = SignStatus.IN_PROGRESS,
                norwegianBankIdResponse = NorwegianBankIdResponse(response.redirectUrl)
            )
            is StartNorwegianAuthenticationResult.Failed -> {
                logger.error("Norwegian authentication failed with errors: ${response.errors}")
                MemberSignResponse(
                    signUUID = response.id,
                    status = SignStatus.FAILED
                )
            }
        }
    }

    fun handleSignResult(result: NorwegianSignResult) {
        when (result) {
            is NorwegianSignResult.Signed -> {
                memberService.norwegianBankIdSignComplete(result.memberId, result.id, result.ssn, result.providerJsonResponse)
            }
            is NorwegianSignResult.Failed -> TODO()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NorwegianSigningService::class.java)
    }
}
