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
    fun getSynaDebtSnapshot(ssn: String): DebtSnapshot {
        val synaDebtSnapshot = synaService.getDebtSnapshot(ssn)
        return DebtSnapshot.from(synaDebtSnapshot, ssn)
    }

    fun getPersonDebtSnapshot(ssn: String): DebtSnapshot {
        try {
            personService.createPerson(ssn)
        } finally {
            personService.checkDebt(ssn)
            return personService.getPerson(ssn)!!.debtSnapshots.last()
        }
    }
}
