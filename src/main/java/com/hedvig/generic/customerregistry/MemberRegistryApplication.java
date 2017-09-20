package com.hedvig.generic.customerregistry;

import com.hedvig.generic.customerregistry.externalEvents.KafkaProperties;
import org.axonframework.config.EventHandlingConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@SpringBootApplication
@EnableConfigurationProperties(KafkaProperties.class)
public class MemberRegistryApplication {

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
}
