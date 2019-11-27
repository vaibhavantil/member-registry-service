package com.hedvig.memberservice.aggregates

enum class FraudulentStatus(val severity: Int) {
    NOT_FRAUD(0),
    SUSPECTED_FRAUD(10),
    CONFIRMED_FRAUD(20)
}
