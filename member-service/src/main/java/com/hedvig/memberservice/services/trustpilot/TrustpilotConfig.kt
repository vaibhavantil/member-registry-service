package com.hedvig.memberservice.services.trustpilot

import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.services.trustpilot.api.TrustpilotClient
import com.hedvig.memberservice.services.trustpilot.api.TrustpilotOauth2Interceptor
import com.hedvig.memberservice.query.MemberRepository
import feign.Feign
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.openfeign.FeignClientsConfiguration
import org.springframework.cloud.openfeign.support.SpringMvcContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestTemplate

@Configuration
@ConfigurationProperties(prefix = "trustpilot.oauth")
@ConditionalOnProperty("trustpilot.customerio-review-links-enabled")
data class TrustpilotConfig(
    var apikey: String = "",
    var secret: String = "",
    var username: String = "",
    var password: String = ""
) {

    @Bean
    fun eventListener(
        notificationService: NotificationService,
        memberRepository: MemberRepository
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
