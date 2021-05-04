package com.hedvig.personservice.persons.domain

import com.hedvig.memberservice.util.logger
import com.hedvig.personservice.debts.model.DebtSnapshot
import com.hedvig.personservice.persons.domain.events.*
import com.hedvig.personservice.persons.model.Whitelisted
import com.hedvig.personservice.maskLastDigitsOfSsn
import com.hedvig.personservice.persons.domain.commands.*
import mu.KotlinLogging
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.Timestamp
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.*

@ProcessingGroup("Person")
@Aggregate
class PersonAggregate() {
    @AggregateIdentifier
    private lateinit var ssn: String
    private lateinit var id: UUID
    private lateinit var debt: DebtSnapshot
    private var latestDateSnapShotFrom: LocalDateTime = LocalDateTime.MIN
    private var lastDebtCheckedAt: Instant = Instant.MIN
    private var whitelisted: Whitelisted? = null

    @CommandHandler
    constructor(command: CreatePersonCommand): this() {
        logger.debug("Person CREATED with ssn=${maskLastDigitsOfSsn(command.ssn)}")
        AggregateLifecycle.apply(PersonCreatedEvent(UUID.randomUUID(), command.ssn))
    }

    @EventSourcingHandler
    fun on(event: PersonCreatedEvent) {
        this.ssn = event.ssn
        this.id = event.id
    }

    @CommandHandler
    fun handle(command: CheckPersonDebtCommand) {
        if (isBeforeFirstFridayOfMonth(lastDebtCheckedAt)) {
            logger.debug("CHECKING debt for ssn=${maskLastDigitsOfSsn(command.ssn)}")
            AggregateLifecycle.apply(CheckPersonDebtEvent(command.ssn))
            return
        }
        if (latestDateSnapShotFrom.isAfter(LocalDateTime.now().minusWeeks(4))) {
            logger.error("Person debt ALREADY checked within 4 week period")
            AggregateLifecycle.apply(PersonDebtAlreadyCheckedEvent(command.ssn))
            return
        }
        logger.debug("CHECKING debt for ssn=${maskLastDigitsOfSsn(command.ssn)}")
        AggregateLifecycle.apply(CheckPersonDebtEvent(command.ssn))
    }

    @CommandHandler
    fun handle(command: SynaDebtCheckedCommand) {
        if (!command.debtSnapshot.fromDateTime.isEqual(latestDateSnapShotFrom)) {
            logger.debug("Debt CHECKED on SYNA-ARKIV for ssn=${maskLastDigitsOfSsn(command.ssn)}")
            AggregateLifecycle.apply(SynaDebtCheckedEvent(
                ssn = command.ssn,
                debtSnapshot = command.debtSnapshot
            ))
        } else {
            logger.debug("Debt ALREADY checked with SYNA-ARKIV for ssn=${maskLastDigitsOfSsn(command.ssn)} from=${command.debtSnapshot.fromDateTime}")
            AggregateLifecycle.apply(SameSynaDebtCheckedEvent(command.ssn))
        }
    }

    @EventSourcingHandler
    fun on(event: SynaDebtCheckedEvent, @Timestamp timestamp: Instant) {
        this.debt = event.debtSnapshot
        this.latestDateSnapShotFrom = event.debtSnapshot.fromDateTime
        this.lastDebtCheckedAt = timestamp
    }

    @CommandHandler
    fun handle(command: WhitelistPersonCommand) {
        if (whitelisted != null) {
            throw Exception("Cannot whitelist person since it is already whitelisted (ssn=${maskLastDigitsOfSsn(ssn)})")
        }
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

    @CommandHandler
    fun handle(command: RemoveWhitelistCommand) {
        if (whitelisted == null) {
            throw Exception("Cannot remove whitelist from person since it is not whitelisted (ssn=${maskLastDigitsOfSsn(ssn)})")
        }
        AggregateLifecycle.apply(WhitelistRemovedEvent(
            ssn = command.ssn,
            removedBy = command.removedBy
        ))
    }

    @EventSourcingHandler
    fun on(event: WhitelistRemovedEvent) {
        this.whitelisted = null
    }

    fun isBeforeFirstFridayOfMonth(now: Instant): Boolean {
        return now < getFirstFridayOfMonth().atZone(ZoneId.of("Europe/Stockholm")).toInstant()
    }

    fun getFirstFridayOfMonth(): LocalDateTime {
        return LocalDateTime.now().withDayOfMonth(1).with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
    }
}
