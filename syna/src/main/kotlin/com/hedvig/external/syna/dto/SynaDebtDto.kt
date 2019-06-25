package com.hedvig.external.syna.dto

import org.javamoney.moneta.Money
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.money.MonetaryAmount

data class SynaDebtDto(
    val debtDate: LocalDate,
    val totalAmountPublicDebt: MonetaryAmount,
    val numberPublicDebts: Int,
    val totalAmountPrivateDebt: MonetaryAmount,
    val numberPrivateDebts: Int
    ) {
    companion object {
        fun from(record: String): SynaDebtDto = SynaDebtDto(
                debtDate = LocalDate.parse(record.substring(10, 18), DateTimeFormatter.ofPattern("yyyyMMdd")),
                totalAmountPublicDebt = Money.of(record.substring(18, 29).toBigDecimal(), "SEK"),
                numberPublicDebts = record.substring(29, 36).toInt(),
                totalAmountPrivateDebt = Money.of(record.substring(36, 47).toBigDecimal(), "SEK"),
                numberPrivateDebts = record.substring(47).toInt()
        )

        fun zero(): SynaDebtDto = SynaDebtDto(
                debtDate = LocalDate.now(),
                totalAmountPublicDebt = Money.of(BigDecimal.ZERO, "SEK"),
                numberPublicDebts = 0,
                totalAmountPrivateDebt = Money.of(BigDecimal.ZERO, "SEK"),
                numberPrivateDebts = 0
        )

    }
}
