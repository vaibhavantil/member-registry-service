package com.hedvig.personservice.debts

import com.hedvig.external.syna.SynaService
import com.hedvig.personservice.debts.model.DebtSnapshot
import com.hedvig.personservice.persons.PersonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class DebtService @Autowired constructor(
        private val synaService: SynaService,
        @Lazy private val personService: PersonService
) {
    fun checkDebtSnapshot(ssn: String): DebtSnapshot {
        val synaDebtSnapshot = synaService.getDebtSnapshot(ssn)
        return DebtSnapshot.from(synaDebtSnapshot, ssn)
    }

    fun getDebtSnapshot(ssn: String): DebtSnapshot {
        try {
            personService.checkDebt(ssn)
        } catch (exception: Exception) {
            personService.createPerson(ssn)
            personService.checkDebt(ssn)
        }
        val person = personService.getPerson(ssn)
        return person!!.debtSnapshots.last()
    }
}
