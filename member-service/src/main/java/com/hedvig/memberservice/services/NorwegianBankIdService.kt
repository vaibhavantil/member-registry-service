package com.hedvig.memberservice.services

import com.hedvig.external.authentication.NorwegianAuthentication
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResult
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.integration.apigateway.ApiGatewayService
import com.hedvig.localization.service.TextKeysLocaleResolver
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import com.hedvig.memberservice.web.dto.RedirectBankIdAuthenticationRequest
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class NorwegianBankIdService(
    private val norwegianAuthentication: NorwegianAuthentication,
    private val commandGateway: CommandGateway,
    private val redisEventPublisher: RedisEventPublisher,
    private val signedMemberRepository: SignedMemberRepository,
    private val apiGatewayService: ApiGatewayService,
    private val memberRepository: MemberRepository,
    private val textKeysLocaleResolver: TextKeysLocaleResolver,
    @Value("\${redirect.authentication.successUrl}")
    private val authenticationSuccessUrl: String,
    @Value("\${redirect.authentication.failUrl}")
    private val authenticationFailUrl: String
) {
    fun authenticate(memberId: Long, request: RedirectBankIdAuthenticationRequest): StartNorwegianAuthenticationResult {
        return norwegianAuthentication.auth(
            NorwegianBankIdAuthenticationRequest(
                memberId.toString(),
                request.personalNumber,
                resolveTwoLetterLanguageFromMember(memberId),
                authenticationSuccessUrl,
                authenticationFailUrl
            )
        )
    }

    fun sign(memberId: String, ssn: String, successUrl: String, failUrl: String) =
        norwegianAuthentication.sign(
            NorwegianBankIdAuthenticationRequest(
                memberId,
                ssn,
                resolveTwoLetterLanguageFromMember(memberId.toLong()),
                successUrl,
                failUrl
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

    private fun resolveTwoLetterLanguageFromMember(memberId: Long): String {
        val acceptLanguage = memberRepository.findById(memberId).get().acceptLanguage
        return getTwoLetterLanguageFromLocale(textKeysLocaleResolver.resolveLocale(acceptLanguage))
    }

    private fun getTwoLetterLanguageFromLocale(locale: Locale) = when (locale.language) {
        "sv" -> "SV"
        "en" -> "EN"
        else -> "NO"
    }
}

