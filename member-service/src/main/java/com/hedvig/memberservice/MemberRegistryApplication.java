package com.hedvig.memberservice;

import com.hedvig.external.billectaAPI.BillectaApi;
import com.hedvig.external.bisnodeBCI.BisnodeClient;
import com.hedvig.memberservice.externalEvents.KafkaProperties;
import org.axonframework.config.EventHandlingConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableConfigurationProperties(KafkaProperties.class)
public class MemberRegistryApplication {

    @Value("${hedvig.bisnode.client.id}")
    String bisnodeClientId = "";

    @Value("${hedvig.bisnode.client.key}")
    String bisnodeClientKey = "";

    @Value("${hedvig.billecta.secure.token}")
    String billectaSecureToken;

    @Value("${hedvig.billecta.creditor.id}")
    String billectaCreditorId;

	public static void main(String[] args) {

	    SpringApplication.run(MemberRegistryApplication.class, args);
	}

    @Autowired
    public void configure(EventHandlingConfiguration config) {

	    config.usingTrackingProcessors();
    }

    @Bean
    public RetryTemplate retryTemplate(){
	    RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(100);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    @Bean
    public BillectaApi buildBillectaApi(){
        return new BillectaApi(billectaCreditorId, billectaSecureToken, new RestTemplate());
    }

    @Bean
    public BisnodeClient bisnodeClient(){
        return new BisnodeClient(bisnodeClientId, bisnodeClientKey, new RestTemplate());
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
