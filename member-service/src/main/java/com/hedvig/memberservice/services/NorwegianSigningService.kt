package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.NorwegianSignResult
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.memberservice.entities.SignStatus
import com.hedvig.memberservice.services.events.SignSessionCompleteEvent
import com.hedvig.memberservice.services.member.MemberService
import com.hedvig.memberservice.services.member.dto.MemberSignResponse
import com.hedvig.memberservice.services.member.dto.NorwegianBankIdResponse
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import com.hedvig.memberservice.web.v2.dto.WebsignRequest
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class NorwegianSigningService(
    private val memberService: MemberService,
    private val norwegianBankIdService: NorwegianBankIdService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val redisEventPublisher: RedisEventPublisher
) {

    @Transactional
    fun startSign(memberId: Long, ssn: String, successUrl: String, failUrl: String): StartNorwegianAuthenticationResult {
        val result = norwegianBankIdService.sign(
            memberId.toString(),
            ssn,
            successUrl,
            failUrl
        )
        redisEventPublisher.onSignSessionUpdate(memberId)
        return result
    }

    fun getSignStatus(memberId: Long) = norwegianBankIdService.getStatus(memberId)

    fun handleSignResult(result: NorwegianSignResult) {
        when (result) {
            is NorwegianSignResult.Signed -> {
                memberService.norwegianBankIdSignComplete(result.memberId, result.id, result.ssn, result.providerJsonResponse)
                applicationEventPublisher.publishEvent(SignSessionCompleteEvent(result.memberId))
            }
            is NorwegianSignResult.Failed -> {
                applicationEventPublisher.publishEvent(SignSessionCompleteEvent(result.memberId))
                redisEventPublisher.onSignSessionUpdate(result.memberId)
            }
        }
    }

    fun notifyContractsCreated(memberId: Long) = norwegianBankIdService.notifyContractsCreated(memberId)

    companion object {
        private val logger = LoggerFactory.getLogger(NorwegianSigningService::class.java)
    }
}
