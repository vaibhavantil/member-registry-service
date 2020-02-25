package com.hedvig.memberservice.web


import com.hedvig.common.DeprecatedException
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.external.bankID.bankIdTypes.CollectResponse
import com.hedvig.external.bankID.bankIdTypes.CollectStatus
import com.hedvig.memberservice.aggregates.exceptions.BankIdReferenceUsedException
import com.hedvig.memberservice.commands.AuthenticationAttemptCommand
import com.hedvig.memberservice.commands.BankIdAuthenticationStatus
import com.hedvig.memberservice.commands.BankIdSignCommand
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.query.CollectRepository
import com.hedvig.memberservice.query.CollectType
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.BankIdService
import com.hedvig.memberservice.services.NorwegianBankIdService
import com.hedvig.memberservice.util.getEndUserIp
import com.hedvig.memberservice.web.dto.APIErrorDTO
import com.hedvig.memberservice.web.dto.BankIdAuthRequest
import com.hedvig.memberservice.web.dto.BankIdAuthResponse
import com.hedvig.memberservice.web.dto.BankIdCollectResponse
import com.hedvig.memberservice.web.dto.BankIdProgressStatus
import com.hedvig.memberservice.web.dto.BankIdProgressStatus.Companion.valueOf
import com.hedvig.memberservice.web.dto.BankIdSignRequest
import com.hedvig.memberservice.web.dto.BankIdSignResponse
import net.logstash.logback.argument.StructuredArguments
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.UnsupportedEncodingException

@RestController
@RequestMapping("/member/")
class AuthController @Autowired constructor(
    private val commandGateway: CommandGateway,
    private val memberRepo: MemberRepository,
    private val signedMemberRepository: SignedMemberRepository,
    private val collectRepo: CollectRepository,
    private val bankIdService: BankIdService,
    private val norwegianBankIdService: NorwegianBankIdService) {

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

    @PostMapping(path = ["bankid/sign"])
    @Throws(UnsupportedEncodingException::class)
    @Deprecated("Use V2")
    fun sign(@RequestHeader(value = "x-forwarded-for", required = false) forwardedIp: String?, @RequestBody request: BankIdSignRequest): ResponseEntity<BankIdSignResponse> {
        throw DeprecatedException("Use V2")
    }

    private fun convertMemberId(memberId: String): Long {
        return try {
            memberId.toLong()
        } catch (e: Exception) {
            throw HttpMessageNotReadableException("Could not parse memberId")
        }
    }

    @PostMapping(path = ["bankid/collect"])
    @Throws(InterruptedException::class)
    fun collect(
        @RequestParam referenceToken: String,
        @RequestHeader(value = "hedvig.token") hid: Long): ResponseEntity<*> {
        MDC.put("memberId", hid.toString())

        log.info("Start collect")

        val collectType = collectRepo.findById(referenceToken).orElse(null)

        if (collectType == null) {
            log.error("ERROR: Oh no! Collect type is null!")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build<Any>()
        }

        return when (collectType.type) {
            CollectType.RequestType.AUTH -> {
                val status = bankIdService.authCollect(referenceToken)
                if (status.status == CollectStatus.complete) {
                    val ssn = status.completionData.user.personalNumber
                    val member = signedMemberRepository.findBySsn(ssn)
                    var currentMemberId = hid
                    if (member.isPresent) {
                        val m = member.get()
                        if (m.id != hid) {
                            commandGateway.sendAndWait<Any>(InactivateMemberCommand(hid))
                        }
                        currentMemberId = m.id
                    }
                    try {
                        val authStatus = BankIdAuthenticationStatus(
                            sSN = status.completionData.user.personalNumber,
                            referenceToken = referenceToken,
                            givenName = status.completionData.user.givenName,
                            surname = status.completionData.user.surname
                        )
                        commandGateway.sendAndWait<Any>(
                            AuthenticationAttemptCommand(
                                id = currentMemberId,
                                bankIdAuthResponse = authStatus)
                        )
                        Thread.sleep(1000L)
                    } catch (e: BankIdReferenceUsedException) {
                        log.info("Old reference token used: ", e)
                        return ResponseEntity.badRequest().body("{\"message\":\"" + e.message + "\"}")
                    }
                    val response = BankIdCollectResponse(
                        bankIdStatus = BankIdProgressStatus.COMPLETE,
                        referenceToken = referenceToken,
                        newMemberId = currentMemberId.toString()
                    )
                    return ResponseEntity.ok().header("Hedvig.Id", currentMemberId.toString()).body(response)
                }

                createResponse(status, referenceToken, hid)
            }
            CollectType.RequestType.SIGN -> {
                val status = bankIdService.signCollect(referenceToken)
                if (status.status == CollectStatus.complete) {
                    val memberEntity = memberRepo.findById(hid)
                    if (memberEntity.isPresent) {
                        commandGateway.sendAndWait<Any>(
                            BankIdSignCommand(
                                id = hid,
                                referenceId = referenceToken,
                                signature = status.completionData.signature,
                                oscpResponse = status.completionData.ocspResponse,
                                personalNumber = status.completionData.user.personalNumber
                            )
                        )
                    }
                }

                createResponse(status, referenceToken, hid)
            }
            else -> {
                ResponseEntity.noContent().build<Any>()
            }
        }
    }

    @PostMapping(path = ["norway/bankid/auth"])
    private fun norwayAuth(@RequestBody request: NorwegianBankIdAuthenticationRequest): ResponseEntity<StartNorwegianAuthenticationResult> {
        return ResponseEntity.ok(norwegianBankIdService.authenticate(request))
    }

    private fun createResponse(collectResponse: CollectResponse, referenceToken: String, hid: Long): ResponseEntity<*> {
        return when (collectResponse.status) {
            CollectStatus.failed -> {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    APIErrorDTO(
                        status = HttpStatus.INTERNAL_SERVER_ERROR,
                        code = when (collectResponse.hintCode) {
                            "expiredTransaction" -> "EXPIRED_TRANSACTION"
                            "certificateErr" -> "CERTIFICATE_ERROR"
                            "userCancel" -> "USER_CANCEL"
                            "cancelled" -> "CANCELLED"
                            "startFailed" -> "START_FAILED"
                            else -> throw IllegalArgumentException("Unknown collect failed hint code: ${collectResponse.hintCode}")
                        },
                        message = ""
                    )
                )
            }
            CollectStatus.pending,
            CollectStatus.complete -> {
                ResponseEntity.ok(
                    BankIdCollectResponse(
                        bankIdStatus = valueOf(collectResponse),
                        referenceToken = referenceToken,
                        newMemberId = hid.toString())
                )
            }
            else -> throw RuntimeException("BankId collection response with no status! Collect response: $collectResponse")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AuthController::class.java)
    }

}
