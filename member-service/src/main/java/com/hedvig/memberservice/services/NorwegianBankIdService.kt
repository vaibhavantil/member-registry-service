package com.hedvig.memberservice.services

import com.hedvig.external.authentication.NorwegianAuthentication
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResult
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.integration.apigateway.ApiGatewayService
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import com.hedvig.memberservice.web.dto.GenericBankIdAuthenticationRequest
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.stereotype.Service

@Service
class NorwegianBankIdService(
    private val norwegianAuthentication: NorwegianAuthentication,
    private val commandGateway: CommandGateway,
    private val redisEventPublisher: RedisEventPublisher,
    private val signedMemberRepository: SignedMemberRepository,
    private val apiGatewayService: ApiGatewayService,
    private val memberRepository: MemberRepository
) {
    fun authenticate(memberId: Long, request: GenericBankIdAuthenticationRequest): StartNorwegianAuthenticationResult {
        val acceptLanguage = memberRepository.findById(memberId).get().acceptLanguage
        return norwegianAuthentication.auth(
            NorwegianBankIdAuthenticationRequest(
                memberId.toString(),
                request.personalNumber,
                acceptLanguage?.toTwoLetterLanguage() ?: "NO"
            )
        )
    }

    fun sign(memberId: String, ssn: String, acceptLanguage: String?) =
        norwegianAuthentication.sign(
            NorwegianBankIdAuthenticationRequest(
                memberId,
                ssn,
                acceptLanguage?.toTwoLetterLanguage() ?: "NO"
            )
        )

    fun completeAuthentication(result: NorwegianAuthenticationResult) {
        when (result) {
            is NorwegianAuthenticationResult.Completed -> {
                val signedMember = signedMemberRepository.findBySsn(result.ssn)
                if (signedMember.isPresent) {
                    if (result.memberId != signedMember.get().id) {
                        commandGateway.sendAndWait<Any>(InactivateMemberCommand(result.memberId))
                        apiGatewayService.reassignMember(result.memberId, signedMember.get().id)
                    }
                    redisEventPublisher.onAuthSessionUpdated(result.memberId, AuthSessionUpdatedEventStatus.SUCCESS)
                } else {
                    redisEventPublisher.onAuthSessionUpdated(result.memberId, AuthSessionUpdatedEventStatus.FAILED)
                }
            }
            is NorwegianAuthenticationResult.Failed ->
                redisEventPublisher.onAuthSessionUpdated(result.memberId, AuthSessionUpdatedEventStatus.FAILED)
        }
    }

    fun getStatus(memberId: Long) = norwegianAuthentication.getStatus(memberId)

    private fun String.toTwoLetterLanguage() = when (this) {
        "sv-SE" -> "SV"
        "en-SE",
        "en-NO" -> "EN"
        else -> "NO"
    }
}

