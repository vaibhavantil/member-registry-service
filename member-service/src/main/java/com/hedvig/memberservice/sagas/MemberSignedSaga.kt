package com.hedvig.memberservice.sagas

import com.hedvig.integration.underwritter.UnderwriterApi
import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.events.MemberSignedWithoutBankId
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
            log.error("Could not notify underwriter about signed member for memberId: {}", e.getId(), ex)
        }

        signingService.productSignConfirmed(e.getReferenceId())
        snsNotificationService.sendMemberSignedNotification(e.getId())
    }

    @SagaEventHandler(associationProperty = "memberId")
    @StartSaga
    @EndSaga
    fun onMemberSignedFromUnderwriterEvent(
        e: MemberSignedWithoutBankId,
        eventMessage: EventMessage<MemberSignedWithoutBankId>
    ) {

        log.debug("Product has already been signed")

        signingService.productSignConfirmed(e.memberId.toString())
        snsNotificationService.sendMemberSignedNotification(e.memberId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(MemberSignedSaga::class.java)
    }
}
