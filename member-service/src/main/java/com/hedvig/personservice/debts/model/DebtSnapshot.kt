package com.hedvig.personservice.debts.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.hedvig.external.syna.dto.SynaDebtCheckDto
import com.hedvig.personservice.persons.model.Person
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
data class DebtSnapshot(
    @Id
    val id: UUID,
    @ManyToOne @JsonIgnore
    var person: Person,
    @ElementCollection
    val paymentDefaults: List<PaymentDefault>,
    @Embedded
    val debt: Debt,
    val checkedAt: Instant,
    val fromDateTime: LocalDateTime
) {
    companion object {
        fun from(synaDebtCheck: SynaDebtCheckDto, ssn: String): DebtSnapshot {
            val debt = Debt.from(synaDebtCheck.debt)
            val paymentDefaults = synaDebtCheck.paymentDefaults.map((PaymentDefault)::from)
            return DebtSnapshot(
                    id = UUID.randomUUID(),
                    person = Person(ssn, mutableListOf()),
                    paymentDefaults = paymentDefaults,
                    debt = debt,
                    checkedAt = Instant.now(),
                    fromDateTime = synaDebtCheck.fromDateTime
            )
        }
    }
}
