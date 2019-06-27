package com.hedvig.personservice.persons.domain.sagas

import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.personservice.debts.DebtService
import com.hedvig.personservice.persons.PersonService
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

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    @EndSaga
    fun on(event: MemberSignedEvent) {
        try {
            personService.checkDebt(event.ssn)
        } catch (exception: Exception) {
            personService.createPerson(event.ssn)
            personService.checkDebt(event.ssn)
        }
    }
}
