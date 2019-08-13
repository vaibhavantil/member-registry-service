package com.hedvig.personservice.debts.web.dtos

import com.hedvig.personservice.debts.model.PaymentDefault
import java.math.BigDecimal

class PaymentDefaultDto(
    val year: Int,
    val week: Int,
    val paymentDefaultType: String,
    val amount: BigDecimal,
    val caseId: String,
    val claimant: String
) {
    companion object {
        fun from(paymentDefault: PaymentDefault) = PaymentDefaultDto(
            year = paymentDefault.year,
            week = paymentDefault.week,
            paymentDefaultType = paymentDefault.paymentDefaultType,
            amount = paymentDefault.amount,
            caseId = paymentDefault.caseId,
            claimant = paymentDefault.claimant
        )
    }
}
