package com.hedvig.personservice.debts

import com.hedvig.memberservice.aggregates.MemberStatus
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.personservice.debts.model.DebtSnapshot
import com.hedvig.personservice.persons.PersonService
import com.hedvig.personservice.persons.model.Flag
import com.hedvig.personservice.persons.model.Person
import com.hedvig.personservice.persons.query.PersonRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class DebtService @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val personRepository: PersonRepository,
    @Lazy private val personService: PersonService
) {
    fun checkPersonDebt(ssn: String) {
        try {
            personService.createPerson(ssn)
        } finally {
            personService.checkDebt(ssn)
        }
    }

    fun checkNonCheckedPersonDebts() {
        val checkedSsn = personRepository.findAll()
            .filter { person -> person.debtSnapshots.isNotEmpty() }
            .map { person -> person.ssn }
        val nonCheckedSsn = memberRepository.findAllByStatusAndSsnNotIn(MemberStatus.SIGNED, checkedSsn)
            .mapNotNull { member -> member.ssn }
            .toSet()
        nonCheckedSsn.forEach { ssn ->
            checkPersonDebt(ssn)
        }
    }

    fun checkAllPersonDebts() {
        val allSignedSsn = memberRepository.findByStatus(MemberStatus.SIGNED)
            .mapNotNull { member -> member.ssn }
            .toSet()
        allSignedSsn.forEach { ssn ->
            checkPersonDebt(ssn)
        }
    }

    companion object {
        fun calculateDebtFlagByPerson(person: Person): Flag {
            val debtSnapshot = person.debtSnapshots.last()
            return getDebtFlag(debtSnapshot)
        }

        private fun getDebtFlag(debtSnapshot: DebtSnapshot): Flag {
            val debt = debtSnapshot.debt
            val totalDebt = debt.totalAmountPrivateDebt + debt.totalAmountPublicDebt
            val paymentDefaults = debtSnapshot.paymentDefaults
            return when {
                totalDebt > BigDecimal.ZERO -> Flag.RED
                paymentDefaults.size > 6 -> Flag.RED
                paymentDefaults.isNotEmpty() -> Flag.AMBER
                else -> Flag.GREEN
            }
        }
    }
}
