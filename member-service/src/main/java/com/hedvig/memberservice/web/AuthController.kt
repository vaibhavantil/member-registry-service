package com.hedvig.memberservice.web


import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import com.hedvig.memberservice.services.BankIdService
import com.hedvig.memberservice.services.signing.zignsec.ZignSecBankIdService
import com.hedvig.memberservice.util.getEndUserIp
import com.hedvig.memberservice.web.dto.BankIdAuthCountry
import com.hedvig.memberservice.web.dto.BankIdAuthRequest
import com.hedvig.memberservice.web.dto.BankIdAuthResponse
import com.hedvig.memberservice.web.dto.GenericBankIdAuthenticationRequest
import com.hedvig.memberservice.web.dto.ZignSecStartDto
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/member/")
class AuthController @Autowired constructor(
    private val bankIdService: BankIdService,
    private val zignSecBankIdService: ZignSecBankIdService
) {

    @PostMapping(path = ["bankid/auth"])
    fun auth(@RequestHeader(value = "x-forwarded-for", required = false) forwardedIp: String?, @RequestBody request: BankIdAuthRequest): ResponseEntity<BankIdAuthResponse> {
        MDC.put("memberId", request.memberId)
        log.info(
            "Auth request for with memberId: ${request.memberId}", StructuredArguments.value("memberId", request.memberId))

        val memberId = convertMemberId(request.memberId)

        val endUserIp = forwardedIp
            .getEndUserIp("Header 'x-forwarded-for' was not included when calling AuthController auth! MemberId:$memberId")

        val status = bankIdService.auth(memberId, endUserIp)

        val response = BankIdAuthResponse(
            autoStartToken = status.autoStartToken,
            referenceToken = status.orderRef
        )

        return ResponseEntity.ok(response)
    }

    private fun convertMemberId(memberId: String): Long {
        return try {
            memberId.toLong()
        } catch (e: Exception) {
            throw HttpMessageNotReadableException("Could not parse memberId")
        }
    }

    @PostMapping(path = ["/{country}/bankid/auth"])
    private fun auth(
        @RequestHeader("hedvig.token") memberId: Long,
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?,
        @RequestHeader("Authorization") authorization: String,
        @PathVariable("country") country: BankIdAuthCountry,
        @RequestBody request: GenericBankIdAuthenticationRequest
    ): ResponseEntity<ZignSecStartDto> {
        MDC.put("memberId", memberId.toString())

        val result = zignSecBankIdService.authenticate(
            memberId = memberId,
            personalNumber = request.personalNumber,
            market = when (country) {
                BankIdAuthCountry.norway -> ZignSecAuthenticationMarket.NORWAY
                BankIdAuthCountry.denmark -> ZignSecAuthenticationMarket.DENMARK
            },
            acceptLanguage = acceptLanguage
        )

        return when (result) {
            is StartZignSecAuthenticationResult.Success ->
                ResponseEntity.ok(ZignSecStartDto(result.redirectUrl))
            is StartZignSecAuthenticationResult.StaticRedirect ->
                ResponseEntity.ok(ZignSecStartDto(result.redirectUrl))
            is StartZignSecAuthenticationResult.Failed -> {
                log.warn("ZignSec authentication start failed, codes = ${result.errors.map { it.code }}")
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AuthController::class.java)
    }

}

