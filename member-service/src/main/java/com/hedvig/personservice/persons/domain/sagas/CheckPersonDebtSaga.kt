package com.hedvig.personservice.persons.domain.sagas

import com.hedvig.personservice.debts.DebtService
import com.hedvig.personservice.persons.domain.commands.SynaDebtCheckedCommand
import com.hedvig.personservice.persons.domain.events.CheckPersonDebtEvent
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired

@Saga
class CheckPersonDebtSaga {
    @Transient
    @Autowired
    lateinit var debtService: DebtService

    @Transient
    @Autowired
    lateinit var commandGateway: CommandGateway

    @StartSaga
    @SagaEventHandler(associationProperty = "ssn")
    @EndSaga
    fun on(event: CheckPersonDebtEvent) {
        val debtSnapshot = debtService.getSynaDebtSnapshot(event.ssn)
        commandGateway.sendAndWait<Void>(SynaDebtCheckedCommand(event.ssn, debtSnapshot))
    }
}
