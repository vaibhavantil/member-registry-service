package com.hedvig.memberservice.sagas

import com.hedvig.integration.underwriter.UnderwriterApi
import com.hedvig.integration.underwriter.dtos.SignMethod
import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.events.MemberSignedWithoutBankId
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import com.hedvig.memberservice.services.SNSNotificationService
import com.hedvig.memberservice.services.SigningService
import com.hedvig.memberservice.services.UnderwriterSigningService
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
    fun onMemberSignedEvent(e: MemberSignedEvent, eventMessage: EventMessage<MemberSignedEvent>) {
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
    fun onMemberSignedFromUnderwriterEvent(
        e: MemberSignedWithoutBankId,
        eventMessage: EventMessage<MemberSignedWithoutBankId>
    ) {
        log.debug("Product has already been signed [MemberId: ${e.memberId}]")

        signingService.productSignConfirmed(e.memberId)
        snsNotificationService.sendMemberSignedNotification(e.memberId)
    }

    @SagaEventHandler(associationProperty = "memberId")
    @StartSaga
    @EndSaga
    fun onNorwegianMemberSignedEvent(
        e: NorwegianMemberSignedEvent,
        eventMessage: EventMessage<MemberSignedWithoutBankId>
    ) {
        val isUnderwriterHandlingSession = e.referenceId?.let {
            underwriterSigningService.isUnderwriterHandlingSignSession(e.referenceId)
        } ?: false

        if (isUnderwriterHandlingSession) {
            try {
                underwriterSigningService.norwegianBankIdSignSessionWasCompleted(e.referenceId!!)
            } catch (ex: RuntimeException) {
                log.error("Could not complete norwegian bank id signing session in about signed member [MemberId: ${e.memberId}] Exception $ex")
            }
        } else {
            try {
                underwriterApi.memberSigned(e.memberId.toString(), "", "", "")
            } catch (ex: RuntimeException) {
                log.error("Could not notify underwriter about signed member [MemberId: ${e.memberId}] Exception $ex")
            }
        }

        signingService.productSignConfirmed(e.memberId)
        signingService.scheduleContractsCreatedJob(e.memberId, SignMethod.NORWEGIAN_BANK_ID)
        snsNotificationService.sendMemberSignedNotification(e.memberId)
    }

    //todo onDanishMemberSignedEvent

    companion object {
        private val log = LoggerFactory.getLogger(MemberSignedSaga::class.java)
    }
}
