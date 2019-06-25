package com.hedvig.external.syna

import org.springframework.ws.client.core.support.WebServiceGatewaySupport
import syna.ObjectFactory
import syna.QueryResponse

class SynaClient: WebServiceGatewaySupport() {

    private val factory = ObjectFactory()

    fun getSynaResponse(ssn: String): QueryResponse {
        val query = factory.createQuery()
        query.id = when(ssn.length == 10) {
            true -> ssn
            false -> ssn.substring(2)
        }
        return webServiceTemplate.marshalSendAndReceive(query) as QueryResponse
    }
}
