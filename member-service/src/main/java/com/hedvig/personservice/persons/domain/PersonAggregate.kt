package com.hedvig.personservice.persons.domain

import com.hedvig.personservice.persons.domain.commands.CheckPersonDebtCommand
import com.hedvig.personservice.persons.domain.commands.CreatePersonCommand
import com.hedvig.personservice.persons.domain.commands.SynaDebtCheckedCommand
import com.hedvig.personservice.persons.domain.commands.WhitelistPersonCommand
import com.hedvig.personservice.persons.domain.events.*
import com.hedvig.personservice.persons.model.Person
import com.hedvig.personservice.persons.model.Whitelisted
import com.hedvig.personservice.maskLastDigitsOfSsn
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle
import org.axonframework.eventhandling.Timestamp
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.*

@Aggregate
class PersonAggregate() {
    @AggregateIdentifier
    private lateinit var ssn: String
    private lateinit var id: UUID
    private lateinit var person: Person
    private var latestDateSnapShotFrom: LocalDateTime = LocalDateTime.MIN
    private var lastDebtCheckedAt: Instant = Instant.MIN
    private var whitelisted: Whitelisted? = null

    private val logger = KotlinLogging.logger { }

    @CommandHandler
    constructor(command: CreatePersonCommand): this() {
        logger.info { "Person CREATED with ssn=${maskLastDigitsOfSsn(command.ssn)}" }
        AggregateLifecycle.apply(PersonCreatedEvent(UUID.randomUUID(), command.ssn))
    }

    @EventSourcingHandler
    fun on(event: PersonCreatedEvent) {
        this.ssn = event.ssn
        this.id = event.id
        this.person = Person(event.id, event.ssn, mutableListOf())
    }

    @CommandHandler
    fun handle(command: CheckPersonDebtCommand) {
        if (isBeforeFirstFridayOfMonth(lastDebtCheckedAt)) {
            logger.info { "CHECKING debt for ssn=${maskLastDigitsOfSsn(command.ssn)}" }
            AggregateLifecycle.apply(CheckPersonDebtEvent(id, command.ssn))
            return
        }
        if (latestDateSnapShotFrom.isAfter(LocalDateTime.now().minusWeeks(4))) {
            logger.error { "Person debt ALREADY checked within 4 week period" }
            AggregateLifecycle.apply(PersonDebtAlreadyCheckedEvent(command.ssn))
            return
        }
        logger.info { "CHECKING debt for ssn=${maskLastDigitsOfSsn(command.ssn)}" }
        AggregateLifecycle.apply(CheckPersonDebtEvent(id, command.ssn))
    }

    @CommandHandler
    fun handle(command: SynaDebtCheckedCommand) {
        if (!command.debtSnapshot.fromDateTime.isEqual(latestDateSnapShotFrom)) {
            logger.info { "Debt CHECKED on SYNA-ARKIV for ssn=${maskLastDigitsOfSsn(command.ssn)}" }
            AggregateLifecycle.apply(SynaDebtCheckedEvent(
                ssn = command.ssn,
                debtSnapshot = command.debtSnapshot
            ))
        } else {
            logger.info { "Debt ALREADY checked with SYNA-ARKIV for ssn=${maskLastDigitsOfSsn(command.ssn)} from=${command.debtSnapshot.fromDateTime}" }
            AggregateLifecycle.apply(SameSynaDebtCheckedEvent(command.ssn))
        }
    }

    @EventSourcingHandler
    fun on(event: SynaDebtCheckedEvent, @Timestamp timestamp: Instant) {
        this.person.debtSnapshots.add(event.debtSnapshot)
        this.latestDateSnapShotFrom = event.debtSnapshot.fromDateTime
        this.lastDebtCheckedAt = timestamp
    }

    @CommandHandler
    fun handle(command: WhitelistPersonCommand) {
        AggregateLifecycle.apply(PersonWhitelistedEvent(
            command.ssn,
            command.whitelistedBy
        ))
    }

    @EventSourcingHandler
    fun on(event: PersonWhitelistedEvent, @Timestamp timestamp: Instant) {
        this.whitelisted = Whitelisted(
            whitelistedAt = timestamp,
            whitelistedBy = event.whitelistedBy
        )
    }

    fun isBeforeFirstFridayOfMonth(now: Instant): Boolean {
        return now < getFirstFridayOfMonth().atZone(ZoneId.of("Europe/Stockholm")).toInstant()
    }

    fun getFirstFridayOfMonth(): LocalDateTime {
        return LocalDateTime.now().withDayOfMonth(1).with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
    }
}
