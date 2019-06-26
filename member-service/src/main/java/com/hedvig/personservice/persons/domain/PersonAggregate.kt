package com.hedvig.personservice.persons.domain

import com.hedvig.personservice.persons.domain.commands.CheckPersonDebtCommand
import com.hedvig.personservice.persons.domain.commands.CreatePersonCommand
import com.hedvig.personservice.persons.domain.commands.SynaDebtCheckedCommand
import com.hedvig.personservice.persons.domain.events.*
import com.hedvig.personservice.persons.model.Person
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import java.time.LocalDateTime

@Aggregate
class PersonAggregate() {
    @AggregateIdentifier
    private lateinit var ssn: String
    private lateinit var person: Person
    private var latestDateSnapShotFrom: LocalDateTime = LocalDateTime.MIN

    private val logger = KotlinLogging.logger { }

    @CommandHandler
    constructor(command: CreatePersonCommand): this() {
        logger.info { "Person CREATED with ssn=${command.ssn}" }
        AggregateLifecycle.apply(PersonCreatedEvent(command.ssn))
    }

    @EventSourcingHandler
    fun on(event: PersonCreatedEvent) {
        this.ssn = event.ssn
        this.person = Person(event.ssn, mutableListOf())
    }

    @CommandHandler
    fun handle(command: CheckPersonDebtCommand) {
        if (latestDateSnapShotFrom.isAfter(LocalDateTime.now().minusWeeks(4))) {
            logger.error { "Person debt ALREADY checked within 4 week period" }
            AggregateLifecycle.apply(PersonDebtAlreadyCheckedEvent(command.ssn))
            return
        }
        logger.info { "CHECKING debt for ssn=${command.ssn}" }
        AggregateLifecycle.apply(CheckPersonDebtEvent(command.ssn))
    }

    @CommandHandler
    fun handle(command: SynaDebtCheckedCommand) {
        if (!command.debtSnapshot.fromDateTime.isEqual(latestDateSnapShotFrom)) {
            logger.info { "Debt CHECKED on SYNA-ARKIV for ssn ${command.ssn}" }
            AggregateLifecycle.apply(SynaDebtCheckedEvent(command.ssn, command.debtSnapshot))
        } else {
            logger.info { "Debt ALREADY checked with SYNA-ARKIV for ssn ${command.ssn} from=${command.debtSnapshot.fromDateTime}" }
            AggregateLifecycle.apply(SameSynaDebtCheckedEvent(command.ssn))
        }
    }

    @EventSourcingHandler
    fun on(event: SynaDebtCheckedEvent) {
        this.person.debtSnapshots.add(event.debtSnapshot)
        this.latestDateSnapShotFrom = event.debtSnapshot.fromDateTime
    }
}
