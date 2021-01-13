package com.hedvig.memberservice.services.signing.zignsec

import com.hedvig.external.authentication.dto.ZignSecSignResult
import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import com.hedvig.memberservice.services.events.SignSessionCompleteEvent
import com.hedvig.memberservice.services.member.MemberService
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class ZignSecSigningService(
    private val memberService: MemberService,
    private val zignSecBankIdService: ZignSecBankIdService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val redisEventPublisher: RedisEventPublisher
) {

    @Transactional
    fun startSign(memberId: Long, ssn: String, successUrl: String, failUrl: String, zignSecAuthenticationMarket: ZignSecAuthenticationMarket): StartZignSecAuthenticationResult {
        val result = zignSecBankIdService.sign(
            memberId.toString(),
            ssn,
            successUrl,
            failUrl,
            zignSecAuthenticationMarket
        )
        redisEventPublisher.onSignSessionUpdate(memberId)
        return result
    }

    fun getSignStatus(memberId: Long) = zignSecBankIdService.getStatus(memberId)

    fun handleSignResult(result: ZignSecSignResult) {
        when (result) {
            is ZignSecSignResult.Signed -> {
                memberService.signComplete(
                    result.memberId,
                    result.id,
                    result.ssn,
                    result.providerJsonResponse,
                    result.authenticationMethod,
                    result.firstName,
                    result.lastName
                )
                applicationEventPublisher.publishEvent(SignSessionCompleteEvent(result.memberId))
            }
            is ZignSecSignResult.Failed -> {
                applicationEventPublisher.publishEvent(SignSessionCompleteEvent(result.memberId))
                redisEventPublisher.onSignSessionUpdate(result.memberId)
            }
        }
    }

    fun notifyContractsCreated(memberId: Long) = zignSecBankIdService.notifyContractsCreated(memberId)
}
