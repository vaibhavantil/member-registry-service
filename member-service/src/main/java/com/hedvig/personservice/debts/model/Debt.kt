package com.hedvig.personservice.debts.model

import com.hedvig.external.syna.dto.SynaDebtDto
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Embeddable

@Embeddable
data class Debt(
    val debtDate: LocalDate,
    val totalAmountPublicDebt: BigDecimal,
    val numberPublicDebts: Int,
    val totalAmountPrivateDebt: BigDecimal,
    val numberPrivateDebts: Int
) {
    companion object {
        fun from(synaDebt: SynaDebtDto): Debt = Debt(
                debtDate = synaDebt.debtDate,
                totalAmountPublicDebt = synaDebt.totalAmountPublicDebt.number.doubleValueExact().toBigDecimal(),
                numberPublicDebts = synaDebt.numberPublicDebts,
                totalAmountPrivateDebt = synaDebt.totalAmountPrivateDebt.number.doubleValueExact().toBigDecimal(),
                numberPrivateDebts = synaDebt.numberPrivateDebts
        )
    }
}
