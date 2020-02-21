package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.NorwegianSignResult
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.external.event.NorwegianSignEvent
import com.hedvig.external.zignSec.ZignSecServiceImpl
import com.hedvig.memberservice.entities.SignStatus
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.events.SignSessionCompleteEvent
import com.hedvig.memberservice.services.member.MemberService
import com.hedvig.memberservice.services.member.dto.MemberSignResponse
import com.hedvig.memberservice.services.member.dto.NorwegianBankIdResponse
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import com.hedvig.memberservice.web.v2.dto.WebsignRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class NorwegianSigningService(
    private val memberRepository: MemberRepository,
    private val memberService: MemberService,
    private val norwegianBankIdService: NorwegianBankIdService,
    private val redisEventPublisher: RedisEventPublisher
    ) {

    @Transactional
    fun startSign(memberId: Long, request: WebsignRequest): MemberSignResponse {

        val acceptLanguage = memberRepository.findById(memberId).get().acceptLanguage
        val response = norwegianBankIdService.sign(
            memberId.toString(),
            request.ssn,
            acceptLanguage
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
                redisEventPublisher.onSignSessionComplete(SignSessionCompleteEvent(result.memberId))
            }
            is NorwegianSignResult.Failed -> redisEventPublisher.onAuthSessionUpdated(result.memberId, AuthSessionUpdatedEventStatus.FAILED)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NorwegianSigningService::class.java)
    }
}
