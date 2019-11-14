package com.hedvig.personservice.persons

import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.personservice.debts.DebtService
import com.hedvig.personservice.persons.domain.commands.BlacklistPersonCommand
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

    fun blacklistPerson(ssn: String, blacklistedBy: String) {
        commandGateway.sendAndWait<Void>(BlacklistPersonCommand(ssn, blacklistedBy))
    }

    fun whitelistPersonByMemberId(memberId: String, whitelistedBy: String) {
        val member = memberRepository.findById(memberId.toLong()).get()
        whitelistPerson(member.ssn, whitelistedBy)
    }

    fun blacklistPersonByMemberId(memberId: String, blacklistedBy: String) {
        val member = memberRepository.findById(memberId.toLong()).get()
        blacklistPerson(member.ssn, blacklistedBy)
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

    companion object {
        fun getAllFlags(person: Person): PersonFlags = PersonFlags(
            debtFlag = DebtService.getDebtFlagByPerson(person)
        )

        fun getFlag(person: Person): Flag {
            val personFlags = getAllFlags(person)
            return when(personFlags.debtFlag) {
                Flag.GREEN -> Flag.GREEN
                Flag.AMBER -> Flag.GREEN
                Flag.RED -> Flag.RED
            }
        }
    }
}
