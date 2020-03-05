package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.NorwegianSignResult
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.memberservice.entities.SignSession
import com.hedvig.memberservice.entities.SignStatus
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.events.SignSessionCompleteEvent
import com.hedvig.memberservice.services.member.MemberService
import com.hedvig.memberservice.services.member.dto.MemberSignResponse
import com.hedvig.memberservice.services.member.dto.NorwegianBankIdResponse
import com.hedvig.memberservice.web.v2.dto.WebsignRequest
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.lang.NonNull
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class NorwegianSigningService(
    private val memberService: MemberService,
    private val norwegianBankIdService: NorwegianBankIdService,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    fun startWebSign(memberId: Long, request: WebsignRequest): MemberSignResponse {
        return when (val response = startSign(memberId, request.ssn)) {
            is StartNorwegianAuthenticationResult.Success -> MemberSignResponse(
                signId = -1L,
                status = SignStatus.IN_PROGRESS,
                norwegianBankIdResponse = NorwegianBankIdResponse(response.redirectUrl)
            )
            is StartNorwegianAuthenticationResult.Failed -> {
                logger.error("Norwegian authentication failed with errors: ${response.errors}")
                MemberSignResponse(
                    status = SignStatus.FAILED
                )
            }
        }
    }

    @Transactional
    fun startSign(memberId: Long, ssn: String?) = norwegianBankIdService.sign(
        memberId.toString(),
        ssn
    )

    fun getSignStatus(memberId: Long) = norwegianBankIdService.getStatus(memberId)

    fun handleSignResult(result: NorwegianSignResult) {
        when (result) {
            is NorwegianSignResult.Signed -> {
                memberService.norwegianBankIdSignComplete(result.memberId, result.id, result.ssn, result.providerJsonResponse)
                applicationEventPublisher.publishEvent(SignSessionCompleteEvent(result.memberId))
            }
            is NorwegianSignResult.Failed -> applicationEventPublisher.publishEvent(SignSessionCompleteEvent(result.memberId))
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NorwegianSigningService::class.java)
    }
}
