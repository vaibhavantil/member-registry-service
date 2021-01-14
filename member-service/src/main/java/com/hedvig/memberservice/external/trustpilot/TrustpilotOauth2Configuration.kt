package com.hedvig.memberservice.external.trustpilot

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class TrustpilotOauth2Configuration(
    @Value("\${trustpilot.oauth.basepath}")
    val basePath: String,
    @Value("\${trustpilot.oauth.apikey}")
    val apikey: String,
    @Value("\${trustpilot.oauth.secret}")
    val secret: String,
    @Value("\${trustpilot.oauth.username}")
    val username: String,
    @Value("\${trustpilot.oauth.password}")
    val password: String
)
