package com.hedvig.memberservice.external.trustpilot

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.Base64Utils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.time.Instant

class TrustpilotOauth2Interceptor(
    val template: RestTemplate,
    val config: TrustpilotOauth2Configuration
) : RequestInterceptor {

    private var accessTokenExpiryTime: Instant = Instant.now()
    private var refreshTokenExpiryTime: Instant = Instant.now()

    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    override fun apply(template: RequestTemplate) {
        if (Instant.now().isAfter(accessTokenExpiryTime)) {
            authorize()
        }

        template.header("Authorization", "Bearer $accessToken")
    }

    private fun authorize() {
        if (Instant.now().isBefore(refreshTokenExpiryTime)) {
            refreshAccessToken(refreshToken)
        } else {
            createTokens()
        }
    }

    private fun refreshAccessToken(refreshToken: String) {
        val response = call("/refresh", LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "refresh_token")
            add("refresh_token", refreshToken)
        })
        setTokens(response)
    }

    private fun createTokens() {
        val response = call("/accesstoken", LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "password")
            add("username", config.username)
            add("password", config.password)
        })
        setTokens(response)
    }

    private fun setTokens(response: TokenResponse) {
        accessToken = response.access_token
        refreshToken = response.refresh_token
        accessTokenExpiryTime = Instant.now().plusSeconds(response.expires_in)
        refreshTokenExpiryTime = Instant.now().plusSeconds(response.refresh_token_expires_in)
    }

    private fun call(path: String, body: MultiValueMap<String, String>): TokenResponse {
        val url = "${config.basePath}$path"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val encodedAuth = Base64Utils.encodeToString("${config.apikey}:${config.secret}".toByteArray())
        headers.add("Authorization", "Basic $encodedAuth")

        val request = HttpEntity(body, headers)

        val response = template.exchange(
            url, HttpMethod.POST, request, TokenResponse::class.java
        )

        if (response.statusCode.is2xxSuccessful) {
            return response.body!!
        }
        throw HttpClientErrorException(response.statusCode)
    }

    data class TokenResponse(
        val access_token: String,
        val refresh_token: String,
        val expires_in: Long,
        val refresh_token_expires_in: Long
    )
}
