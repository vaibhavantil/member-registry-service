package com.hedvig.memberservice.config;

import com.hedvig.external.bisnodeBCI.BisnodeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BisnodeConfig {

    @Value("${hedvig.bisnode.client.id}")
    String bisnodeClientId;

    @Value("${hedvig.bisnode.client.key}")
    String bisnodeClientKey = "";

    @Bean
    public BisnodeClient bisnodeClient(RestTemplate restTemplate) {
        return new BisnodeClient(bisnodeClientId, bisnodeClientKey, restTemplate);
    }
}
