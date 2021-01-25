package com.hedvig.memberservice.services.signing.zignsec

import com.hedvig.external.authentication.ZignSecAuthentication
import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult
import com.hedvig.integration.apigateway.ApiGatewayService
import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import com.hedvig.memberservice.web.dto.GenericBankIdAuthenticationRequest
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.jupiter.api.Test
import org.springframework.core.env.Environment
import java.util.Optional
import java.util.UUID

class ZignSecBankIdServiceAuthenticateTest {

    private val zignSecAuthentication: ZignSecAuthentication = mockk()
    private val commandGateway: CommandGateway = mockk()
    private val redisEventPublisher: RedisEventPublisher = mockk()
    private val signedMemberRepository: SignedMemberRepository = mockk()
    private val apiGatewayService: ApiGatewayService = mockk()
    private val memberRepository: MemberRepository = mockk()
    private val env: Environment = mockk()

    private val sut = ZignSecBankIdService(
        zignSecAuthentication,
        commandGateway,
        redisEventPublisher,
        signedMemberRepository,
        apiGatewayService,
        memberRepository,
        env,
        "",
        ""
    )

    @Test
    fun `authenticate market norway with ssn returns response from zignSecAuthentication`() {
        val responseUrl = "url form zignSecAuthentication"
        every { zignSecAuthentication.auth(any()) } returns StartZignSecAuthenticationResult.Success(UUID.randomUUID(), responseUrl)
        every { memberRepository.findById(any()) } returns Optional.empty()

        val response = sut.authenticate(
            memberId,
            GenericBankIdAuthenticationRequest("ssn"),
            ZignSecAuthenticationMarket.NORWAY,
            null
        )

        assertThat(response).isInstanceOf(StartZignSecAuthenticationResult.Success::class.java)
        assertThat((response as StartZignSecAuthenticationResult.Success).redirectUrl).isEqualTo(responseUrl)
    }

    @Test
    fun `authenticate market denmark without ssn returns response from zignSecAuthentication`() {
        val responseUrl = "url form zignSecAuthentication"
        every { zignSecAuthentication.auth(any()) } returns StartZignSecAuthenticationResult.Success(UUID.randomUUID(), responseUrl)
        every { memberRepository.findById(any()) } returns Optional.empty()

        val response = sut.authenticate(
            memberId,
            GenericBankIdAuthenticationRequest(null),
            ZignSecAuthenticationMarket.DENMARK,
            null
        )

        assertThat(response).isInstanceOf(StartZignSecAuthenticationResult.Success::class.java)
        assertThat((response as StartZignSecAuthenticationResult.Success).redirectUrl).isEqualTo(responseUrl)
    }

    @Test
    fun `authenticate market norway without ssn returns static redirect`() {
        every { memberRepository.findById(any()) } returns Optional.empty()
        every { env.activeProfiles } returns arrayOf()

        val response = sut.authenticate(
            memberId,
            GenericBankIdAuthenticationRequest(null),
            ZignSecAuthenticationMarket.NORWAY,
            null
        )

        assertThat(response).isInstanceOf(StartZignSecAuthenticationResult.StaticRedirect::class.java)
        assertThat((response as StartZignSecAuthenticationResult.StaticRedirect).redirectUrl).isEqualTo(stagingEnglishUrl)
    }

    @Test
    fun `authenticate market norway without ssn and active profile of production returns static redirect of prod url`() {
        every { memberRepository.findById(any()) } returns Optional.empty()
        every { env.activeProfiles } returns arrayOf("production")

        val response = sut.authenticate(
            memberId,
            GenericBankIdAuthenticationRequest(null),
            ZignSecAuthenticationMarket.NORWAY,
            null
        )

        assertThat(response).isInstanceOf(StartZignSecAuthenticationResult.StaticRedirect::class.java)
        assertThat((response as StartZignSecAuthenticationResult.StaticRedirect).redirectUrl).isEqualTo(prodEnglishUrl)
    }

    @Test
    fun `authenticate market norway without ssn and accept languge nb-NO and active profile of production returns static redirect of prod url`() {
        every { memberRepository.findById(any()) } returns Optional.empty()
        every { env.activeProfiles } returns arrayOf("production")

        val response = sut.authenticate(
            memberId,
            GenericBankIdAuthenticationRequest(null),
            ZignSecAuthenticationMarket.NORWAY,
            "nb-NO"
        )

        assertThat(response).isInstanceOf(StartZignSecAuthenticationResult.StaticRedirect::class.java)
        assertThat((response as StartZignSecAuthenticationResult.StaticRedirect).redirectUrl).isEqualTo(prodNorwegianUrl)
    }

    @Test
    fun `authenticate with no member accept language nb-NO returns norwegian url`() {
        every { memberRepository.findById(any()) } returns Optional.empty()
        every { env.activeProfiles } returns arrayOf()

        val response = sut.authenticate(
            memberId,
            GenericBankIdAuthenticationRequest(null),
            ZignSecAuthenticationMarket.NORWAY,
            "nb-NO"
        )

        assertThat(response).isInstanceOf(StartZignSecAuthenticationResult.StaticRedirect::class.java)
        assertThat((response as StartZignSecAuthenticationResult.StaticRedirect).redirectUrl).isEqualTo(stagingNorwegianUrl)
    }

    @Test
    fun `authenticate with member with picked locale nb_NO returns norwegian url`() {
        every { memberRepository.findById(any()) } returns Optional.of(createMemberWithPickedLocale(PickedLocale.nb_NO))
        every { env.activeProfiles } returns arrayOf()

        val response = sut.authenticate(
            memberId,
            GenericBankIdAuthenticationRequest(null),
            ZignSecAuthenticationMarket.NORWAY,
            null
        )

        assertThat(response).isInstanceOf(StartZignSecAuthenticationResult.StaticRedirect::class.java)
        assertThat((response as StartZignSecAuthenticationResult.StaticRedirect).redirectUrl).isEqualTo(stagingNorwegianUrl)
    }

    @Test
    fun `authenticate with no member accept language en-NO returns english url`() {
        every { memberRepository.findById(any()) } returns Optional.empty()
        every { env.activeProfiles } returns arrayOf()

        val response = sut.authenticate(
            memberId,
            GenericBankIdAuthenticationRequest(null),
            ZignSecAuthenticationMarket.NORWAY,
            "en-NO"
        )

        assertThat(response).isInstanceOf(StartZignSecAuthenticationResult.StaticRedirect::class.java)
        assertThat((response as StartZignSecAuthenticationResult.StaticRedirect).redirectUrl).isEqualTo(stagingEnglishUrl)
    }

    @Test
    fun `authenticate with member with picked locale en_NO returns english url`() {
        every { memberRepository.findById(any()) } returns Optional.of(createMemberWithPickedLocale(PickedLocale.en_NO))
        every { env.activeProfiles } returns arrayOf()

        val response = sut.authenticate(
            memberId,
            GenericBankIdAuthenticationRequest(null),
            ZignSecAuthenticationMarket.NORWAY,
            null
        )

        assertThat(response).isInstanceOf(StartZignSecAuthenticationResult.StaticRedirect::class.java)
        assertThat((response as StartZignSecAuthenticationResult.StaticRedirect).redirectUrl).isEqualTo(stagingEnglishUrl)
    }

    @Test
    fun `authenticate with no member and no accept language returns english url`() {
        every { memberRepository.findById(any()) } returns Optional.empty()
        every { env.activeProfiles } returns arrayOf()

        val response = sut.authenticate(
            memberId,
            GenericBankIdAuthenticationRequest(null),
            ZignSecAuthenticationMarket.NORWAY,
            null
        )

        assertThat(response).isInstanceOf(StartZignSecAuthenticationResult.StaticRedirect::class.java)
        assertThat((response as StartZignSecAuthenticationResult.StaticRedirect).redirectUrl).isEqualTo(stagingEnglishUrl)
    }

    private fun createMemberWithPickedLocale(pickedLocale: PickedLocale) = MemberEntity().apply {
        this.pickedLocale = pickedLocale
    }

    companion object {
        private const val memberId = 1234L
        private const val stagingEnglishUrl = "https://www.dev.hedvigit.com/no-en/login"
        private const val stagingNorwegianUrl = "https://www.dev.hedvigit.com/no/login"
        private const val prodEnglishUrl = "https://www.hedvig.com/no-en/login"
        private const val prodNorwegianUrl = "https://www.hedvig.com/no/login"
    }
}
