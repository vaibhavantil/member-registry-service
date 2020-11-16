package com.hedvig.memberservice.jobs

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class BankidMetrics(
    registry: MeterRegistry) {

    private val authStartedCounter = registry.counter("bankid.auth.started.count")
    private val authFailedCounter =  registry.counter("bankid.auth.failed.count")
    private val authCompletedCounter =  registry.counter("bankid.auth.completed.count")

    private val signStartedCounter = registry.counter("bankid.sign.started.count")
    private val signFailedCounter = registry.counter("bankid.sign.failed.count")
    private val signCompletedCounter = registry.counter("bankid.sign.completed.count")
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
        signCompletedCounter
    }

    fun failBankIdSign() {
        signFailedCounter.increment()
    }
}
