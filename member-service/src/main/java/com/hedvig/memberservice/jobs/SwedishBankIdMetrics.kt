package com.hedvig.memberservice.jobs

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class SwedishBankIdMetrics(
    private val registry: MeterRegistry) {

    val hintCodes = listOf("userCancel", "cancelled", "expiredTransaction", "certificateErr", "startFailed")
    val defaultHintCode = "unknown"

    private fun getHintCodeOrDefault(hintCode:String) = if (hintCode.contains(hintCode)) hintCode else defaultHintCode

    private fun registerCounterWithTags(
        name: String,
        tags: List<String>
    ): Map<String, Counter> =
        tags.plus(defaultHintCode).map { tag -> tag to  registry.counter(name, "hintcode", tag)}.toMap()

    private val authStartedCounter = registry.counter( "bankid.se.auth.started.count")
    private val authFailedCounters =  registerCounterWithTags("bankid.se.auth.failed.count", hintCodes)
    private val authCompletedCounter =  registry.counter("bankid.se.auth.completed.count")

    private val signStartedCounter = registry.counter("bankid.se.sign.started.count")
    private val signFailedCounters = registerCounterWithTags("bankid.se.sign.failed.count", hintCodes)
    private val signCompletedCounter = registry.counter("bankid.se.sign.completed.count")
    fun startBankIdV2Auth() {
        authStartedCounter.increment()
    }

    fun failedBankIdV2Auth(hintCode: String) {
        authFailedCounters[getHintCodeOrDefault(hintCode)]?.increment()
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

    fun failBankIdSign(hintCode: String) {
        signFailedCounters[getHintCodeOrDefault(hintCode)]?.increment()
    }
}
