package com.hedvig.personservice.debts.model

import com.hedvig.external.syna.dto.SynaPaymentDefaultDto
import java.math.BigDecimal
import javax.persistence.Embeddable

@Embeddable
data class PaymentDefault(
    val year: Int,
    val week: Int,
    val paymentDefaultType: String,
    val amount: BigDecimal,
    val caseId: String,
    val claimant: String
) {
    companion object {
        fun from(synaPaymentDefault: SynaPaymentDefaultDto): PaymentDefault = PaymentDefault(
                year = synaPaymentDefault.year.value,
                week = synaPaymentDefault.week,
                paymentDefaultType = synaPaymentDefault.paymentDefaultType,
                amount = synaPaymentDefault.amount.number.doubleValueExact().toBigDecimal(),
                caseId = synaPaymentDefault.caseId,
                claimant = synaPaymentDefault.claimant
        )
    }
}
