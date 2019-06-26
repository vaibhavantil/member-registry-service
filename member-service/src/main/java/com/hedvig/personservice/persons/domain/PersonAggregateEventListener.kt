package com.hedvig.personservice.persons.domain

import com.hedvig.personservice.debts.query.DebtSnapshotRepository
import com.hedvig.personservice.persons.domain.events.PersonCreatedEvent
import com.hedvig.personservice.persons.domain.events.SynaDebtCheckedEvent
import com.hedvig.personservice.persons.model.Person
import com.hedvig.personservice.persons.query.PersonRepository
import org.axonframework.eventhandling.EventHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PersonAggregateEventListener @Autowired constructor(
        private val personRepository: PersonRepository,
        private val debtSnapshotRepository: DebtSnapshotRepository
) {
    @EventHandler
    fun on(event: PersonCreatedEvent) {
        val person = Person(event.ssn, mutableListOf())
        personRepository.save(person)
    }

    @EventHandler
    fun on(event: SynaDebtCheckedEvent) {
        debtSnapshotRepository.save(event.debtSnapshot)
    }
}
