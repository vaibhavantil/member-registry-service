package com.hedvig.personservice.persons

import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.personservice.debts.DebtService
import com.hedvig.personservice.persons.domain.commands.CheckPersonDebtCommand
import com.hedvig.personservice.persons.domain.commands.CreatePersonCommand
import com.hedvig.personservice.persons.model.Flag
import com.hedvig.personservice.persons.model.Person
import com.hedvig.personservice.persons.model.PersonFlags
import com.hedvig.personservice.persons.query.PersonRepository
import com.hedvig.personservice.safeSsn
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.lang.IllegalStateException
import java.math.BigDecimal

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

    fun getPersonOrNull(ssn: String): Person? {
        val person = personRepository.findById(ssn)
        if (person.isPresent) return person.get()
        return null
    }

    fun getPersonOrNullByMemberId(memberId: String): Person? {
        val member = memberRepository.findById(memberId.toLong())
        if (!member.isPresent) return null
        return getPersonOrNull(member.get().ssn)
    }

    fun getFlagsByMemberId(memberId: String): PersonFlags {
        val person = getPersonOrNullByMemberId(memberId)
            ?: throw IllegalStateException("Could not get flags of member since no person exist for it (memberId=$memberId)")
        return getFlags(person)
    }

    companion object {
        fun getFlags(person: Person): PersonFlags = PersonFlags(
            debtFlag = DebtService.getDebtFlagByPerson(person)
        )
    }
}
