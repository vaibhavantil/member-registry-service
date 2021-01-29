package com.hedvig.memberservice.services.trustpilot

import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.trustpilot.api.TrustpilotClient
import com.hedvig.memberservice.services.trustpilot.api.TrustpilotOauth2Interceptor
import feign.Feign
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.openfeign.support.SpringMvcContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
@ConditionalOnProperty("trustpilot.customerio-review-links-enabled")
class TrustpilotConfig {

    @Bean
    fun eventListener(
        notificationService: NotificationService,
        memberRepository: MemberRepository,
        @Value("\${trustpilot.oauth.apikey}") apikey: String,
        @Value("\${trustpilot.oauth.secret}") secret: String,
        @Value("\${trustpilot.oauth.username}") username: String,
        @Value("\${trustpilot.oauth.password}") password: String
    ): CustomerIOTrustpilotEventListener {
        return CustomerIOTrustpilotEventListener(
            notificationService = notificationService,
            memberRepository =  memberRepository,
            trustpilotReviewService = TrustpilotReviewServiceImpl(
                trustpilotClient = Feign.builder()
                    .contract(SpringMvcContract()) // enables spring annotations like @PostMapping
                    .requestInterceptor(TrustpilotOauth2Interceptor(RestTemplate(), apikey, secret, username, password))
                    .target(TrustpilotClient::class.java, "https://invitations-api.trustpilot.com/v1/private")
            )
        )
    }
}
