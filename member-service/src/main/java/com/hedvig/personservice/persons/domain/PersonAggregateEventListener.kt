package com.hedvig.personservice.persons.domain

import com.hedvig.personservice.debts.query.DebtSnapshotRepository
import com.hedvig.personservice.persons.domain.events.WhitelistRemovedEvent
import com.hedvig.personservice.persons.domain.events.PersonCreatedEvent
import com.hedvig.personservice.persons.domain.events.PersonWhitelistedEvent
import com.hedvig.personservice.persons.domain.events.SynaDebtCheckedEvent
import com.hedvig.personservice.persons.model.Person
import com.hedvig.personservice.persons.model.Whitelisted
import com.hedvig.personservice.persons.query.PersonRepository
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class PersonAggregateEventListener @Autowired constructor(
    private val personRepository: PersonRepository,
    private val debtSnapshotRepository: DebtSnapshotRepository
) {
    @EventHandler
    fun on(event: PersonCreatedEvent) {
        val person = Person(event.id, event.ssn, mutableListOf())
        personRepository.save(person)
    }

    @EventHandler
    fun on(event: SynaDebtCheckedEvent) {
        debtSnapshotRepository.save(event.debtSnapshot)
    }

    @EventHandler
    fun on(event: PersonWhitelistedEvent, @Timestamp timestamp: Instant) {
        val person = personRepository.findBySsn(event.ssn)!!
        person.whitelisted = Whitelisted(
            whitelistedAt = timestamp,
            whitelistedBy = event.whitelistedBy
        )
        personRepository.save(person)
    }

    @EventHandler
    fun on(event: WhitelistRemovedEvent) {
        val person = personRepository.findBySsn(event.ssn)!!
        person.whitelisted = null
        personRepository.save(person)
    }
}
