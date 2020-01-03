package com.hedvig.memberservice.services

import com.hedvig.external.bankID.bankIdRest.BankIdRestApi
import com.hedvig.external.bankID.bankIdRestTypes.CollectRequest
import com.hedvig.external.bankID.bankIdRestTypes.OrderAuthRequest
import com.hedvig.memberservice.query.CollectType.RequestType

import com.hedvig.external.bankID.bankIdRestTypes.CollectResponse
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse
import com.hedvig.memberservice.query.CollectRepository
import com.hedvig.memberservice.query.CollectType
import java.io.UnsupportedEncodingException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BankIdService(
    private val bankIdRestApi: BankIdRestApi,
    private val collectRepository: CollectRepository
) {
    private val log = LoggerFactory.getLogger(BankIdService::class.java)

    fun auth(memberId: Long?, endUserIp: String?): OrderResponse {
        val status = bankIdRestApi.auth(OrderAuthRequest(endUserIp))
        log.info(
            "Started bankId AUTH autostart:{}, reference:{}",
            status.autoStartToken,
            status.orderRef)
        trackAuthToken(status.orderRef, memberId)
        return status
    }

    @Throws(UnsupportedEncodingException::class)
    fun sign(ssn: String, userMessage: String, memberId: Long?, endUserIp: String?): OrderResponse {
        val status = bankIdRestApi.sign(ssn, endUserIp, userMessage)
        trackSignToken(status.orderRef, memberId)
        return status
    }

    private fun trackSignToken(referenceToken: String, memberId: Long?) {
        trackReferenceToken(referenceToken, RequestType.SIGN, memberId)
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
        return bankIdRestApi.collect(CollectRequest(referenceToken))
    }

    fun signCollect(referenceToken: String): CollectResponse {
        return bankIdRestApi.collect(CollectRequest(referenceToken))
    }

}
