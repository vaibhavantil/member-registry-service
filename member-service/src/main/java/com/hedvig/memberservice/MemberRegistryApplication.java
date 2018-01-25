package com.hedvig.memberservice;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.hedvig.external.billectaAPI.BillectaApi;
import com.hedvig.external.billectaAPI.BillectaApiFake;
import com.hedvig.external.billectaAPI.BillectaApiImpl;
import com.hedvig.external.billectaAPI.BillectaClient;
import com.hedvig.external.bisnodeBCI.BisnodeClient;
import com.hedvig.memberservice.aggregates.MemberAggregate;
import com.hedvig.memberservice.externalEvents.KafkaProperties;
import com.hedvig.memberservice.services.bankid.BankIdAdapter;
import com.hedvig.memberservice.services.bankid.BankIdApi;
import com.hedvig.memberservice.services.CashbackService;
import org.axonframework.config.EventHandlingConfiguration;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.spring.eventsourcing.SpringPrototypeAggregateFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.MailSender;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@SpringBootApplication()
@EnableConfigurationProperties(KafkaProperties.class)
@EnableFeignClients({"com.hedvig.external.billectaAPI", "com.hedvig.memberservice.externalApi.prouctsPricing"})
public class MemberRegistryApplication {

    @Value("${hedvig.bisnode.client.id}")
    String bisnodeClientId = "";

    @Value("${hedvig.bisnode.client.key}")
    String bisnodeClientKey = "";

    @Value("${hedvig.billecta.secure.token}")
    String billectaSecureToken;

    @Value("${hedvig.billecta.creditor.id}")
    String billectaCreditorId;

    @Value("${hedvig.billecta.url}")
    String baseUrl;

	public static void main(String[] args) {

	    SpringApplication.run(MemberRegistryApplication.class, args);
	}

    @Autowired
    MailSender mailSender;

    @Autowired
    AmazonSimpleEmailService ses;

    @Autowired
    public void configure(EventHandlingConfiguration config) {
	    //config.usingTrackingProcessors();
    }

    @Bean("bankId")
    @Primary
    BankIdApi bankIdApi(com.hedvig.external.bankID.BankIdApi impl) {
	    return new BankIdAdapter(impl);
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
    public  ScheduledExecutorService executorService() {
        return new ScheduledThreadPoolExecutor(5);
    }

    @Bean
    public BillectaApi buildBillectaApi(BillectaClient billectaClient, ScheduledExecutorService executorService){
        //return new BillectaApiFake();
        return new BillectaApiImpl(billectaCreditorId, billectaSecureToken, new RestTemplate(), baseUrl, billectaClient, executorService);
    }

    @Bean
    public BisnodeClient bisnodeClient(){
        return new BisnodeClient(bisnodeClientId, bisnodeClientKey, new RestTemplate());
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    @Scope("prototype")
    public MemberAggregate memberAggregate(BisnodeClient bisnodeClient, CashbackService cashbackService) {
        return new MemberAggregate(bisnodeClient, cashbackService);
    }

    @Bean
    public AggregateFactory<MemberAggregate> memberAggregateFactory() {
        SpringPrototypeAggregateFactory<MemberAggregate> springPrototypeAggregateFactory = new SpringPrototypeAggregateFactory<>();
        springPrototypeAggregateFactory.setPrototypeBeanName("memberAggregate");

        return springPrototypeAggregateFactory;
    }
}
