package com.hedvig.memberservice.services.signing.zignsec

import com.hedvig.external.authentication.ZignSecAuthentication
import com.hedvig.external.authentication.dto.ZignSecAuthenticationResult
import com.hedvig.external.authentication.dto.ZignSecBankIdAuthenticationRequest
import com.hedvig.integration.apigateway.ApiGatewayService
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.commands.ZignSecSuccessfulAuthenticationCommand
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import com.hedvig.memberservice.web.dto.GenericBankIdAuthenticationRequest
import com.hedvig.resolver.LocaleResolver
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class ZignSecBankIdService(
    private val zignSecAuthentication: ZignSecAuthentication,
    private val commandGateway: CommandGateway,
    private val redisEventPublisher: RedisEventPublisher,
    private val signedMemberRepository: SignedMemberRepository,
    private val apiGatewayService: ApiGatewayService,
    private val memberRepository: MemberRepository,
    @Value("\${redirect.authentication.successUrl}")
    private val authenticationSuccessUrl: String,
    @Value("\${redirect.authentication.failUrl}")
    private val authenticationFailUrl: String
) {
    fun authenticate(
        memberId: Long,
        request: GenericBankIdAuthenticationRequest,
        zignSecAuthenticationMarket: ZignSecAuthenticationMarket) = zignSecAuthentication.auth(
        ZignSecBankIdAuthenticationRequest(
            memberId.toString(),
            request.personalNumber,
            resolveTwoLetterLanguageFromMember(memberId),
            authenticationSuccessUrl,
            authenticationFailUrl,
            zignSecAuthenticationMarket.getAuthenticationMethod()
        )
    )

    fun sign(
        memberId: String,
        ssn: String, successUrl: String,
        failUrl: String,
        zignSecAuthenticationMarket: ZignSecAuthenticationMarket) =
        zignSecAuthentication.sign(
            ZignSecBankIdAuthenticationRequest(
                memberId,
                ssn,
                resolveTwoLetterLanguageFromMember(memberId.toLong()),
                successUrl,
                failUrl,
                zignSecAuthenticationMarket.getAuthenticationMethod()
            )
        )

    fun completeAuthentication(result: ZignSecAuthenticationResult) {
        when (result) {
            is ZignSecAuthenticationResult.Completed -> {
                val signedMember = signedMemberRepository.findBySsn(result.ssn)
                if (signedMember.isPresent) {
                    if (result.memberId != signedMember.get().id) {
                        commandGateway.sendAndWait<Any>(InactivateMemberCommand(result.memberId))
                        apiGatewayService.reassignMember(result.memberId, signedMember.get().id)
                    }
                    commandGateway.sendAndWait<Any>(ZignSecSuccessfulAuthenticationCommand)
                    redisEventPublisher.onAuthSessionUpdated(result.memberId, AuthSessionUpdatedEventStatus.SUCCESS)
                } else {
                    redisEventPublisher.onAuthSessionUpdated(result.memberId, AuthSessionUpdatedEventStatus.FAILED)
                }
            }
            is ZignSecAuthenticationResult.Failed ->
                redisEventPublisher.onAuthSessionUpdated(result.memberId, AuthSessionUpdatedEventStatus.FAILED)
        }
    }

    fun getStatus(memberId: Long) = zignSecAuthentication.getStatus(memberId)

    fun notifyContractsCreated(memberId: Long) = zignSecAuthentication.notifyContractsCreated(memberId)

    private fun resolveTwoLetterLanguageFromMember(memberId: Long): String {
        val acceptLanguage = memberRepository.findById(memberId).get().acceptLanguage
        return getTwoLetterLanguageFromLocale(LocaleResolver.resolveLocale(acceptLanguage))
    }

    private fun getTwoLetterLanguageFromLocale(locale: Locale) = when (locale.language) {
        "sv" -> "SV"
        "en" -> "EN"
        "da" -> "DA"
        else -> "NO"
    }
}

