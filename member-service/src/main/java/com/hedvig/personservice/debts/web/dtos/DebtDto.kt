package com.hedvig.personservice.debts.web.dtos

import com.hedvig.personservice.debts.model.DebtSnapshot
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

class DebtDto(
    val paymentDefaults: List<PaymentDefaultDto>,
    val debtDate: LocalDate,
    val totalAmountPublicDebt: BigDecimal,
    val numberPublicDebts: Int,
    val totalAmountPrivateDebt: BigDecimal,
    val numberPrivateDebts: Int,
    val checkedAt: Instant,
    val fromDateTime: LocalDateTime
) {
    companion object {
        fun from(debtSnapshot: DebtSnapshot): DebtDto = DebtDto(
            paymentDefaults = debtSnapshot.paymentDefaults.map((PaymentDefaultDto)::from),
            debtDate = debtSnapshot.debt.debtDate,
            totalAmountPublicDebt = debtSnapshot.debt.totalAmountPublicDebt,
            numberPublicDebts = debtSnapshot.debt.numberPublicDebts,
            totalAmountPrivateDebt = debtSnapshot.debt.totalAmountPrivateDebt,
            numberPrivateDebts = debtSnapshot.debt.numberPrivateDebts,
            checkedAt = debtSnapshot.checkedAt,
            fromDateTime = debtSnapshot.fromDateTime
        )
    }
}
