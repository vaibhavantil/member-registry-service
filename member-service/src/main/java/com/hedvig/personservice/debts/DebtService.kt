package com.hedvig.personservice.debts

import com.hedvig.external.syna.SynaService
import com.hedvig.personservice.debts.model.DebtSnapshot
import com.hedvig.personservice.persons.PersonService
import com.hedvig.personservice.persons.model.Flag
import com.hedvig.personservice.safeSsn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class DebtService @Autowired constructor(
        private val synaService: SynaService,
        @Lazy private val personService: PersonService
) {
    fun checkPersonDebt(ssn: String) {
        try {
            personService.createPerson(ssn)
        } finally {
            personService.checkDebt(ssn)
            return
        }
    }

    fun getSynaDebtSnapshot(ssn: String): DebtSnapshot {
        val synaDebtSnapshot = synaService.getDebtSnapshot(ssn)
        return DebtSnapshot.from(synaDebtSnapshot, ssn)
    }

    fun getPersonDebtSnapshot(ssn: String): DebtSnapshot {
        try {
            personService.createPerson(ssn)
        } finally {
            personService.checkDebt(ssn)
            val person = personService.getPersonOrNull(ssn)
            if (person != null) return person.debtSnapshots.last()
            throw Exception("Could not get DebtSnapshot for person (ssn=${safeSsn(ssn)})")
        }
    }

    fun getDebtFlag(ssn: String): Flag {
        val debtSnapshot = getPersonDebtSnapshot(ssn)
        val debt = debtSnapshot.debt
        val totalDebt = debt.totalAmountPrivateDebt + debt.totalAmountPrivateDebt
        val paymentDefaults = debtSnapshot.paymentDefaults
        return when {
            totalDebt > BigDecimal.ZERO -> Flag.RED
            paymentDefaults.size > 2 -> Flag.RED
            paymentDefaults.isNotEmpty() -> Flag.AMBER
            else -> Flag.GREEN
        }
    }
}
