package com.hedvig.personservice.debts.web.dtos

import com.hedvig.personservice.debts.model.DebtSnapshot
import org.javamoney.moneta.Money
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import javax.money.MonetaryAmount

class DebtDto(
    val paymentDefaults: List<PaymentDefaultDto>,
    val debtDate: LocalDate,
    val totalAmountPublicDebt: MonetaryAmount,
    val numberPublicDebts: Int,
    val totalAmountPrivateDebt: MonetaryAmount,
    val numberPrivateDebts: Int,
    val totalAmountDebt: MonetaryAmount,
    val totalNumberOfDebts: Int,
    val checkedAt: Instant,
    val fromDateTime: LocalDateTime
) {
    companion object {
        fun from(debtSnapshot: DebtSnapshot): DebtDto = DebtDto(
            paymentDefaults = debtSnapshot.paymentDefaults.map((PaymentDefaultDto)::from),
            debtDate = debtSnapshot.debt.debtDate,
            totalAmountPublicDebt = Money.of(debtSnapshot.debt.totalAmountPublicDebt, "SEK"),
            numberPublicDebts = debtSnapshot.debt.numberPublicDebts,
            totalAmountPrivateDebt = Money.of(debtSnapshot.debt.totalAmountPrivateDebt, "SEK"),
            numberPrivateDebts = debtSnapshot.debt.numberPrivateDebts,
            totalAmountDebt = Money.of(debtSnapshot.debt.totalAmountPublicDebt + debtSnapshot.debt.totalAmountPrivateDebt, "SEK"),
            totalNumberOfDebts = debtSnapshot.debt.numberPublicDebts + debtSnapshot.debt.numberPrivateDebts,
            checkedAt = debtSnapshot.checkedAt,
            fromDateTime = debtSnapshot.fromDateTime
        )
    }
}
