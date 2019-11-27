package com.hedvig.personservice.persons

import com.hedvig.memberservice.aggregates.FraudulentStatus
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.personservice.debts.DebtService
import com.hedvig.personservice.persons.domain.commands.RemoveWhitelistCommand
import com.hedvig.personservice.persons.domain.commands.CheckPersonDebtCommand
import com.hedvig.personservice.persons.domain.commands.CreatePersonCommand
import com.hedvig.personservice.persons.domain.commands.WhitelistPersonCommand
import com.hedvig.personservice.persons.model.Flag
import com.hedvig.personservice.persons.model.Person
import com.hedvig.personservice.persons.model.PersonFlags
import com.hedvig.personservice.persons.query.PersonRepository
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class PersonService @Autowired constructor(
        private val commandGateway: CommandGateway,
        private val personRepository: PersonRepository,
        private val memberRepository: MemberRepository,
        @Lazy private val debtService: DebtService
) {

    fun createPerson(ssn: String) {
        commandGateway.sendAndWait<Void>(CreatePersonCommand(ssn))
    }

    fun checkDebt(ssn: String) {
        commandGateway.sendAndWait<Void>(CheckPersonDebtCommand(ssn))
    }

    fun whitelistPerson(ssn: String, whitelistedBy: String) {
        commandGateway.sendAndWait<Void>(WhitelistPersonCommand(ssn, whitelistedBy))
    }

    fun removeWhitelist(ssn: String, removedBy: String) {
        commandGateway.sendAndWait<Void>(RemoveWhitelistCommand(ssn, removedBy))
    }

    fun whitelistPersonByMemberId(memberId: String, whitelistedBy: String) {
        val member = memberRepository.findById(memberId.toLong()).get()
        whitelistPerson(member.ssn, whitelistedBy)
    }

    fun removeWhitelistByMemberId(memberId: String, removedBy: String) {
        val member = memberRepository.findById(memberId.toLong()).get()
        removeWhitelist(member.ssn, removedBy)
    }

    fun getPersonOrNull(ssn: String): Person? {
        return personRepository.findBySsn(ssn)
    }

    fun getPersonOrNullByMemberId(memberId: String): Person? {
        val memberMaybe = memberRepository.findById(memberId.toLong())
        if (!memberMaybe.isPresent) return null
        val member = memberMaybe.get()
        if (member.ssn == null) return null
        return getPersonOrNull(member.ssn)
    }

    fun getPersonFlag(person: Person): Flag {
        val personFlags = getAllPersonFlags(person)
        return calculateOverallFlag(personFlags)
    }

    fun getAllPersonFlags(person: Person): PersonFlags {
        val membersOfPerson = memberRepository.findBySsn(person.ssn)
        return calculateAllFlags(person, membersOfPerson)
    }

    companion object {
        fun calculateAllFlags(person: Person, members: List<MemberEntity>): PersonFlags = PersonFlags(
            debtFlag = DebtService.calculateDebtFlagByPerson(person),
            fraudFlag = calculateFraudFlag(members)
        )

        fun calculateOverallFlag(personFlags: PersonFlags): Flag {
            return when(personFlags.debtFlag) {
                Flag.GREEN -> Flag.GREEN
                Flag.AMBER -> Flag.GREEN
                Flag.RED -> Flag.RED
            }
        }

        fun calculateFraudFlag(members: List<MemberEntity>): Flag {
            return when (members.maxBy { member -> member.fraudulentStatus?.severity ?: -1 }!!.fraudulentStatus) {
                FraudulentStatus.CONFIRMED_FRAUD -> Flag.RED
                FraudulentStatus.SUSPECTED_FRAUD -> Flag.AMBER
                FraudulentStatus.NOT_FRAUD -> Flag.GREEN
                else -> Flag.GREEN
            }
        }
    }
}
