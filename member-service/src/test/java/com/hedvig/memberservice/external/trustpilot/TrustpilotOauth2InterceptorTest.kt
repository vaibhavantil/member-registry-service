package com.hedvig.memberservice.external.trustpilot

import com.hedvig.memberservice.services.trustpilot.api.TrustpilotOauth2Interceptor
import com.hedvig.memberservice.services.trustpilot.api.TrustpilotOauth2Interceptor.TokenResponse
import feign.RequestTemplate
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

internal class TrustpilotOauth2InterceptorTest {

    @MockK
    lateinit var template: RestTemplate

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `successful authorization is attached to request`() {
        val token = "access-token"
        every {
            template.exchange(any<String>(), any(), any(), TokenResponse::class.java)
        } returns ResponseEntity(
            TokenResponse(token, "", 0, 0),
            HttpStatus.OK
        )

        val sut = TrustpilotOauth2Interceptor(
            template, "apikey", "secret", "user", "pw"
        )
        val request = RequestTemplate()
        sut.apply(request)

        assertThat(request.headers()["Authorization"]).first().isEqualTo("Bearer $token")
    }

    @Test
    fun `first call is token creation`() {
        val url = slot<String>()
        val body = slot<HttpEntity<MultiValueMap<String, String>>>()
        every {
            template.exchange(capture(url), any(), capture(body), TokenResponse::class.java)
        } returns ResponseEntity(
            TokenResponse("", "", 0, 0),
            HttpStatus.OK
        )

        val sut = TrustpilotOauth2Interceptor(
            template, "apikey", "secret", "user", "pw"
        )
        sut.apply(RequestTemplate())

        assertThat(url.captured).endsWith("accesstoken")
        assertThat(body.captured.body!!["username"]?.first()).isEqualTo("user")
        assertThat(body.captured.body!!["password"]?.first()).isEqualTo("pw")
    }

    @Test
    fun `refresh is called if access token is expired`() {
        val url = slot<String>()
        val body = slot<HttpEntity<MultiValueMap<String, String>>>()
        every {
            template.exchange(capture(url), any(), capture(body), TokenResponse::class.java)
        } returns ResponseEntity(
            TokenResponse("", "refresh-token", -1, 10),
            HttpStatus.OK
        )

        val sut = TrustpilotOauth2Interceptor(
            template, "apikey", "secret", "user", "pw"
        )
        sut.apply(RequestTemplate()) // will be /accesstoken
        url.clear()
        body.clear()
        sut.apply(RequestTemplate()) // should be /refresh

        assertThat(url.captured).endsWith("refresh")
        assertThat(body.captured.body!!["refresh_token"]?.first()).isEqualTo("refresh-token")
    }

    @Test
    fun `tokens are created again if refresh token is expired`() {
        val url = slot<String>()
        val body = slot<HttpEntity<MultiValueMap<String, String>>>()
        every {
            template.exchange(capture(url), any(), capture(body), TokenResponse::class.java)
        } returns ResponseEntity(
            TokenResponse("", "", -1, -1),
            HttpStatus.OK
        )

        val sut = TrustpilotOauth2Interceptor(
            template, "apikey", "secret", "user", "pw"
        )
        sut.apply(RequestTemplate()) // will be /accesstoken
        url.clear()
        body.clear()
        sut.apply(RequestTemplate()) // should be /accesstoken again since the expiry duration is negative

        assertThat(url.captured).endsWith("/accesstoken")
        assertThat(body.captured.body!!["username"]?.first()).isEqualTo("user")
        assertThat(body.captured.body!!["password"]?.first()).isEqualTo("pw")
    }
}
