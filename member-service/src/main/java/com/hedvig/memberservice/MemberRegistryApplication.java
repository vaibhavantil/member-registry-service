package com.hedvig.memberservice;

import com.hedvig.common.UUIDGenerator;
import com.hedvig.common.UUIDGeneratorImpl;
import com.hedvig.external.bisnodeBCI.BisnodeClient;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mail.MailSender;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication()
@EnableFeignClients({"com.hedvig.memberservice", "com.hedvig.external.bankID"})
public class MemberRegistryApplication {

  @Value("${hedvig.bisnode.client.id}")
  String bisnodeClientId = "";

  @Value("${hedvig.bisnode.client.key}")
  String bisnodeClientKey = "";
  @Autowired MailSender mailSender;

  public static void main(String[] args) {
    SpringApplication.run(MemberRegistryApplication.class, args);
  }

  @Bean
  public RetryTemplate retryTemplate() {
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
  public BisnodeClient bisnodeClient(RestTemplate restTemplate) {
    return new BisnodeClient(bisnodeClientId, bisnodeClientKey, restTemplate);
  }

  @Bean
  public RestTemplate restTemplate(
      RestTemplateBuilder builder,
      CustomClientHttpRequestInterceptor customClientHttpRequestInterceptor) {
    RestTemplate restTemplate =
        new RestTemplate(
            new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    List<ClientHttpRequestInterceptor> interceptors =
        Collections.singletonList(customClientHttpRequestInterceptor);
    restTemplate.setInterceptors(interceptors);
    return restTemplate;
  }

  @Bean
  public UUIDGenerator uuidGenerator() {
    return new UUIDGeneratorImpl();
  }
}
