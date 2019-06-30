package com.hedvig.personservice.persons.domain.sagas

import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.personservice.debts.DebtService
import com.hedvig.personservice.persons.PersonService
import mu.KLogger
import mu.KotlinLogging
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired

@Saga(configurationBean = "personSignedSagaConfiguration")
class PersonSignedSaga {
    @Autowired
    @Transient
    lateinit var personService: PersonService

    @Transient
    private var actualLogger: KLogger? = null

    private val logger: KLogger
        get() {
            actualLogger = actualLogger ?: KotlinLogging.logger{}
            return actualLogger!!
        }

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    @EndSaga
    fun on(event: MemberSignedEvent) {
        if (event.ssn != null) {
            try {
                personService.checkDebt(event.ssn)
            } catch (exception: Exception) {
                personService.createPerson(event.ssn)
                personService.checkDebt(event.ssn)
            }
        } else {
            logger.error { "Could not check debt for ${event.id} because no SSN present" }
        }
    }
}
