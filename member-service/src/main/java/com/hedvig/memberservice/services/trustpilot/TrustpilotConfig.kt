package com.hedvig.memberservice.services.trustpilot

import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.trustpilot.api.TrustpilotClient
import com.hedvig.memberservice.services.trustpilot.api.TrustpilotOauth2Interceptor
import feign.Contract
import feign.Feign
import feign.codec.Decoder
import feign.codec.Encoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.openfeign.FeignClientsConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestTemplate

@Configuration
@ConditionalOnProperty("trustpilot.customerio-review-links-enabled")
@Import(FeignClientsConfiguration::class)
class TrustpilotConfig(
    private val notificationService: NotificationService,
    private val memberRepository: MemberRepository,
    @Value("\${trustpilot.oauth.apikey}") private val apikey: String,
    @Value("\${trustpilot.oauth.secret}") private val secret: String,
    @Value("\${trustpilot.oauth.username}") private val username: String,
    @Value("\${trustpilot.oauth.password}") private val password: String
) {

    @Bean
    fun eventListener(
        encoder: Encoder,
        decoder: Decoder,
        contract: Contract
    ): CustomerIOTrustpilotEventListener {
        return CustomerIOTrustpilotEventListener(
            notificationService = notificationService,
            memberRepository =  memberRepository,
            trustpilotReviewService = TrustpilotReviewServiceImpl(
                trustpilotClient = Feign.builder()
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .requestInterceptor(TrustpilotOauth2Interceptor(RestTemplate(), apikey, secret, username, password))
                    .target(TrustpilotClient::class.java, "https://invitations-api.trustpilot.com/v1/private")
            )
        )
    }
}
