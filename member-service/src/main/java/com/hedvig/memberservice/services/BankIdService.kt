package com.hedvig.memberservice.services

import com.hedvig.external.bankID.bankId.BankIdApi
import com.hedvig.external.bankID.bankIdTypes.CollectRequest
import com.hedvig.external.bankID.bankIdTypes.OrderAuthRequest
import com.hedvig.memberservice.query.CollectType.RequestType

import com.hedvig.external.bankID.bankIdTypes.CollectResponse
import com.hedvig.external.bankID.bankIdTypes.OrderResponse
import com.hedvig.memberservice.query.CollectRepository
import com.hedvig.memberservice.query.CollectType
import java.io.UnsupportedEncodingException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BankIdService(
    private val bankIdApi: BankIdApi,
    private val collectRepository: CollectRepository
) {
    private val log = LoggerFactory.getLogger(BankIdService::class.java)

    fun auth(memberId: Long?, endUserIp: String?): OrderResponse {
        val status = bankIdApi.auth(OrderAuthRequest(endUserIp))
        log.info(
            "Started bankId AUTH autostart:{}, reference:{}",
            status.autoStartToken,
            status.orderRef)
        trackAuthToken(status.orderRef, memberId)
        return status
    }

    private fun trackAuthToken(referenceToken: String, memberId: Long?) {
        trackReferenceToken(referenceToken, RequestType.AUTH, memberId)
    }

    private fun trackReferenceToken(referenceToken: String, sign: RequestType, memberId: Long?) {
        val ct = CollectType()
        ct.token = referenceToken
        ct.type = sign
        ct.memberId = memberId
        collectRepository.save(ct)
    }

    fun authCollect(referenceToken: String): CollectResponse {
        return bankIdApi.collect(CollectRequest(referenceToken))
    }

    fun signCollect(referenceToken: String): CollectResponse {
        return bankIdApi.collect(CollectRequest(referenceToken))
    }

}
