package com.hedvig.memberservice.jobs

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class SwedishBankIdMetrics(
    registry: MeterRegistry) {

    private val authStartedCounter = registry.counter("bankid.se.auth.started.count")
    private val authFailedCounter =  registry.counter("bankid.se.auth.failed.count")
    private val authCompletedCounter =  registry.counter("bankid.se.auth.completed.count")

    private val signStartedCounter = registry.counter("bankid.se.sign.started.count")
    private val signFailedCounter = registry.counter("bankid.se.sign.failed.count")
    private val signCompletedCounter = registry.counter("bankid.se.sign.completed.count")
    fun startBankIdV2Auth() {
        authStartedCounter.increment()
    }

    fun failedBankIdV2Auth() {
        authFailedCounter.increment()
    }

    fun completeBankIdV2Auth() {
        authCompletedCounter.increment()
    }

    fun startBankIdSign() {
        signStartedCounter.increment()
    }

    fun completeBankIdSign() {
        signCompletedCounter.increment()
    }

    fun failBankIdSign() {
        signFailedCounter.increment()
    }
}
