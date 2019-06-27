package com.hedvig.personservice.persons

import com.hedvig.personservice.debts.DebtService
import com.hedvig.personservice.persons.domain.commands.CheckPersonDebtCommand
import com.hedvig.personservice.persons.domain.commands.CreatePersonCommand
import com.hedvig.personservice.persons.model.Flag
import com.hedvig.personservice.persons.model.Person
import com.hedvig.personservice.persons.query.PersonRepository
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PersonService @Autowired constructor(
        private val commandGateway: CommandGateway,
        private val personRepository: PersonRepository,
        @Lazy private val debtService: DebtService
) {

    fun createPerson(ssn: String) {
        commandGateway.sendAndWait<Void>(CreatePersonCommand(ssn))
    }

    fun checkDebt(ssn: String) {
        commandGateway.sendAndWait<Void>(CheckPersonDebtCommand(ssn))
    }

    fun getPerson(ssn: String): Person? {
        return personRepository.findById(ssn).orElse(null)
    }

    fun getFlag(ssn: String): Flag {
        val debtSnapshot = debtService.getPersonDebtSnapshot(ssn)
        val debt = debtSnapshot.debt
        val totalDebt = debt.totalAmountPrivateDebt + debt.totalAmountPrivateDebt
        val paymentDefaults = debtSnapshot.paymentDefaults
        return when {
            totalDebt > BigDecimal.ZERO -> Flag.RED
            paymentDefaults.size > 3 -> Flag.RED
            paymentDefaults.isNotEmpty() -> Flag.AMBER
            else -> Flag.GREEN
        }
    }
}
