package com.hedvig.external.syna.dto

import org.javamoney.moneta.Money
import java.time.Year
import javax.money.MonetaryAmount

class SynaPaymentDefaultDto(
    val year: Year,
    val week: Int,
    val paymentDefaultType: String,
    val amount: MonetaryAmount,
    val caseId: String,
    val claimant: String
) {
    companion object {
        fun from(record: String): SynaPaymentDefaultDto =
                SynaPaymentDefaultDto(
                        year = Year.of(record.substring(10, 14).toInt()),
                        week = record.substring(14, 16).toInt(),
                        paymentDefaultType = record.substring(16, 18),
                        amount = Money.of(record.substring(18, 30).toBigDecimal(), "SEK"),
                        caseId = record.substring(50, 56),
                        claimant = record.substring(56).trim()
                )
    }
}
