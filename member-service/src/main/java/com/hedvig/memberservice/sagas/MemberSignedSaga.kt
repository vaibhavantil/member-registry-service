package com.hedvig.memberservice.sagas

import com.hedvig.integration.underwriter.UnderwriterApi
import com.hedvig.integration.underwriter.dtos.SignMethod
import com.hedvig.memberservice.events.DanishMemberSignedEvent
import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.events.MemberSignedWithoutBankId
import com.hedvig.memberservice.events.MemberSimpleSignedEvent
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import com.hedvig.memberservice.services.SNSNotificationService
import com.hedvig.memberservice.services.signing.SigningService
import com.hedvig.memberservice.services.signing.underwriter.UnderwriterSigningService
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

@Saga(configurationBean = "memberSignedSagaConfiguration")
class MemberSignedSaga {
    @Autowired
    @Transient
    lateinit var underwriterApi: UnderwriterApi

    @Autowired
    @Transient
    lateinit var underwriterSigningService: UnderwriterSigningService

    @Autowired
    @Transient
    lateinit var signingService: SigningService

    @Autowired
    @Transient
    lateinit var snsNotificationService: SNSNotificationService

    @SagaEventHandler(associationProperty = "id")
    @StartSaga
    @EndSaga
    fun onMemberSignedEvent(e: MemberSignedEvent) {
        val isUnderwriterHandlingSession = underwriterSigningService.isUnderwriterHandlingSignSession(UUID.fromString(e.getReferenceId()))
        if (isUnderwriterHandlingSession) {
            try {
                underwriterSigningService.swedishBankIdSignSessionWasCompleted(e.getReferenceId(), e.getSignature(), e.getOscpResponse())
            } catch (ex: RuntimeException) {
                log.error("Could not complete swedish bank id signing session in about signed member [MemberId: ${e.id}] Exception $ex")
            }
        } else {
            try {
                underwriterApi.memberSigned(e.getId().toString(), e.getReferenceId(), e.getSignature(), e.getOscpResponse())
            } catch (ex: RuntimeException) {
                log.error("Could not notify underwriter about signed member [MemberId: ${e.id}] Exception $ex")
            }
        }

        signingService.completeSwedishSession(e.getReferenceId())
        signingService.productSignConfirmed(e.id)
        signingService.scheduleContractsCreatedJob(e.id, SignMethod.SWEDISH_BANK_ID)
        snsNotificationService.sendMemberSignedNotification(e.getId())
    }

    @SagaEventHandler(associationProperty = "memberId")
    @StartSaga
    @EndSaga
    fun onMemberSignedFromUnderwriterEvent(e: MemberSignedWithoutBankId) {
        log.debug("Product has already been signed [MemberId: ${e.memberId}]")

        signingService.productSignConfirmed(e.memberId)
        snsNotificationService.sendMemberSignedNotification(e.memberId)
    }

    @SagaEventHandler(associationProperty = "memberId")
    @StartSaga
    @EndSaga
    fun onNorwegianMemberSignedEvent(e: NorwegianMemberSignedEvent) {
        generifiedUnderwriterSignSession(e.memberId, e.referenceId, SignMethod.NORWEGIAN_BANK_ID) { referenceId ->
            underwriterSigningService.underwriterSignSessionWasCompleted(referenceId)
        }
    }

    @SagaEventHandler(associationProperty = "memberId")
    @StartSaga
    @EndSaga
    fun onDanishMemberSignedEvent(e: DanishMemberSignedEvent) {
        generifiedUnderwriterSignSession(e.memberId, e.referenceId, SignMethod.DANISH_BANK_ID) { referenceId ->
            underwriterSigningService.underwriterSignSessionWasCompleted(referenceId)
        }
    }

    @SagaEventHandler(associationProperty = "memberId")
    @StartSaga
    @EndSaga
    fun onMemberSimpleSignedEvent(
        e: MemberSimpleSignedEvent
    ) {
        generifiedUnderwriterSignSession(e.memberId, e.referenceId, SignMethod.SIMPLE_SIGN) { referenceId ->
            underwriterSigningService.underwriterSignSessionWasCompleted(referenceId)
        }
    }

    private fun generifiedUnderwriterSignSession(
        memberId: Long,
        referenceId: UUID?,
        signMethod: SignMethod,
        underwritingSign: (referenceId: UUID) -> Unit
    ) {
        val isUnderwriterHandlingSession = referenceId?.let {
            underwriterSigningService.isUnderwriterHandlingSignSession(it)
        } ?: false

        if (isUnderwriterHandlingSession) {
            try {
                underwritingSign(referenceId!!)
            } catch (ex: RuntimeException) {
                log.error("Could not complete generified ZignSec bank id signing session in about signed member [MemberId: ${memberId}] Exception $ex")
            }
        } else {
            try {
                underwriterApi.memberSigned(memberId.toString(), "", "", "")
            } catch (ex: RuntimeException) {
                log.error("Could not notify underwriter about signed member [MemberId: ${memberId}] Exception $ex")
            }
        }

        signingService.productSignConfirmed(memberId)
        signingService.scheduleContractsCreatedJob(memberId, signMethod)
        snsNotificationService.sendMemberSignedNotification(memberId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(MemberSignedSaga::class.java)
    }
}
