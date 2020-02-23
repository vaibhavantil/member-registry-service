package com.hedvig.memberservice.sagas

import com.hedvig.integration.underwriter.UnderwriterApi
import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.events.MemberSignedWithoutBankId
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import com.hedvig.memberservice.services.SNSNotificationService
import com.hedvig.memberservice.services.SigningService
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@Saga(configurationBean = "memberSignedSagaConfiguration")
class MemberSignedSaga {
    @Autowired
    @Transient
    lateinit var underwriterApi: UnderwriterApi

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

        try {
            underwriterApi.memberSigned(e.getId().toString(), e.getReferenceId(), e.getSignature(), e.getOscpResponse())
        } catch (ex: RuntimeException) {
            log.error("Could not notify underwriter about signed member [MemberId: ${e.id}] Exception $ex")
        }

        signingService.swedishProductSignConfirmed(e.getReferenceId())
        signingService.productSignConfirmed(e.id)
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

        //FIXME: I can't se when this has ever worked! Please help me!
        signingService.swedishProductSignConfirmed(e.memberId.toString())
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
        try {
            //FIXME: Maybe we should create a new endpoint for this in uw
            underwriterApi.memberSigned(e.memberId.toString(), "", "", "")
        } catch (ex: RuntimeException) {
            log.error("Could not notify underwriter about signed member [MemberId: ${e.memberId}] Exception $ex")
        }

        signingService.productSignConfirmed(e.memberId)
        snsNotificationService.sendMemberSignedNotification(e.memberId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(MemberSignedSaga::class.java)
    }
}
