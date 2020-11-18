package com.hedvig.external

import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod
import com.hedvig.external.authentication.dto.ZignSecBankIdAuthenticationRequest
import com.hedvig.external.zignSec.repository.entitys.ZignSecAuthenticationType
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class Metrics(private val registry:MeterRegistry) {

    private val authRequestSuccessCounters = generateMethodCounters("zignSec.requests.success")
    private val authRequestFailedCounters = generateMethodCounters("zignSec.requests.fail")
    private val sessionFailedCounter = generateMethodCounters("zignSec.session.fail")
    private val sessionSuccessCounter = generateMethodCounters("zignSec.session.success")
    private val sessionStartedCounter = generateMethodCounters("zignSec.session.started")

    fun authRequestFailed(request: ZignSecBankIdAuthenticationRequest, type: ZignSecAuthenticationType) {
        authRequestFailedCounters[request.authMethod]?.get(type)?.increment()
    }

    fun authRequestSuccess(authMethod: ZignSecAuthenticationMethod, type: ZignSecAuthenticationType) {
        sessionStartedCounter[authMethod]?.get(type)?.increment()
        authRequestSuccessCounters[authMethod]?.get(type)?.increment()
    }

    fun signSessionFailed(authenticationMethod: ZignSecAuthenticationMethod, requestType: ZignSecAuthenticationType) {
        sessionFailedCounter[authenticationMethod]?.get(requestType)?.increment()
    }

    fun signSessionSuccess(authenticationMethod: ZignSecAuthenticationMethod, requestType: ZignSecAuthenticationType) {
        sessionSuccessCounter[authenticationMethod]?.get(requestType)?.increment()
    }

    private fun generateMethodCounters(counterName: String): Map<ZignSecAuthenticationMethod, Map<ZignSecAuthenticationType, Counter>> {
        val methods = ZignSecAuthenticationMethod.values()
        return methods
            .map { m -> m to generateTypeCounters(counterName, "method", m.zignSecMethodName) }
            .toMap()
    }

    private fun generateTypeCounters(counterName: String, vararg tags:String): Map<ZignSecAuthenticationType, Counter> {
        val types = ZignSecAuthenticationType.values()
        return types
            .map { t -> t to registry.counter(counterName, "type", t.name.toLowerCase(), *tags)  }
            .toMap()
    }
}
