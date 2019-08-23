package com.hedvig.external.syna

import org.springframework.ws.client.core.support.WebServiceGatewaySupport
import syna.ObjectFactory
import syna.QueryResponse
import java.lang.Exception

class SynaClient: WebServiceGatewaySupport() {

    private val factory = ObjectFactory()

    fun getSynaResponse(ssn: String): QueryResponse {
        val query = factory.createQuery()
        query.id = when (ssn.length) {
            10 -> ssn
            12 -> ssn.substring(2)
            else -> throw(Exception("Invalid SSN length"))
        }
        return webServiceTemplate.marshalSendAndReceive(query) as QueryResponse
    }
}
