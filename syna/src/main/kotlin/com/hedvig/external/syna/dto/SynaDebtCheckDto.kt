package com.hedvig.external.syna.dto

import syna.QueryResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class SynaDebtCheckDto(
        val fromDateTime: LocalDateTime,
        val paymentDefaults: List<SynaPaymentDefaultDto>,
        val debt: SynaDebtDto
) {
    companion object {
        fun from(queryResponse: QueryResponse): SynaDebtCheckDto {
            val fromDateTime = LocalDateTime.parse(
                queryResponse.buildDate.value,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            )
            val paymentDefaultRecords = queryResponse.tables.table[0].records.record
            val paymentDefaults = when (paymentDefaultRecords.isNotEmpty()) {
                true -> paymentDefaultRecords.map((SynaPaymentDefaultDto)::from)
                false -> listOf()
            }
            val debtRecord = queryResponse.tables.table[1].records.record
            return when (debtRecord.isEmpty()) {
                true -> SynaDebtCheckDto(
                        fromDateTime = fromDateTime,
                        paymentDefaults = paymentDefaults,
                        debt = SynaDebtDto.zero()
                )
                false -> SynaDebtCheckDto(
                        fromDateTime = fromDateTime,
                        paymentDefaults = paymentDefaults,
                        debt = SynaDebtDto.from(debtRecord[0])
                )
            }
        }
    }
}
