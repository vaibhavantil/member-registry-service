package com.hedvig.personservice.debts.web.dtos

import com.hedvig.personservice.debts.model.PaymentDefault
import com.hedvig.personservice.debts.model.PaymentDefaultType
import org.javamoney.moneta.Money
import javax.money.MonetaryAmount

class PaymentDefaultDto(
    val year: Int,
    val week: Int,
    val paymentDefaultType: PaymentDefaultType,
    val paymentDefaultTypeText: String,
    val amount: MonetaryAmount,
    val caseId: String,
    val claimant: String
) {
    companion object {
        fun from(paymentDefault: PaymentDefault) = PaymentDefaultDto(
            year = paymentDefault.year,
            week = paymentDefault.week,
            paymentDefaultType = paymentDefault.paymentDefaultType,
            paymentDefaultTypeText= paymentDefault.paymentDefaultType.text,
            amount = Money.of(paymentDefault.amount, "SEK"),
            caseId = paymentDefault.caseId,
            claimant = paymentDefault.claimant
        )
    }
}
