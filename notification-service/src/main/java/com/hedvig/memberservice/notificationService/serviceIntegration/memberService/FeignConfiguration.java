package com.hedvig.memberservice.notificationService.serviceIntegration.memberService;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class FeignConfiguration {

    //@Bean
    //ErrorDecoder errorDecoder(ObjectMapper objectMapper){
    //    return new MemberServiceErrorDecoder(objectMapper);
    //}
}
