package com.hedvig.memberservice.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import lombok.val;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.support.destination.DynamicQueueUrlDestinationResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AWS {

  @Bean
  @Profile("development")
  public AmazonSQSAsync amazonSQS(AWSCredentialsProvider credentialsProvider) {
    val endpoint = "http://localhost:9324";
    val region = "elastcmq";
    return AmazonSQSAsyncClientBuilder.standard()
        .withCredentials(credentialsProvider)
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
        .build();
  }

  @Bean
  @Profile("development")
  public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(
      AmazonSQSAsync amazonSqs) {
    DynamicQueueUrlDestinationResolver dynamicQueueUrlDestinationResolver =
        new DynamicQueueUrlDestinationResolver(amazonSqs);
    dynamicQueueUrlDestinationResolver.setAutoCreate(true);

    SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory =
        new SimpleMessageListenerContainerFactory();
    simpleMessageListenerContainerFactory.setAmazonSqs(amazonSqs);
    simpleMessageListenerContainerFactory.setDestinationResolver(
        dynamicQueueUrlDestinationResolver);
    return simpleMessageListenerContainerFactory;
  }

  @Bean
  public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSqs) {
    return new QueueMessagingTemplate(amazonSqs);
  }

  @Bean
  AWSCredentialsProvider credentialsProvider() {
    return new DefaultAWSCredentialsProviderChain();
  }

  @Bean
  @Profile(Profiles.PRODUCTION)
  public NotificationMessagingTemplate notificationTemplate(AmazonSNS amazonSNS) {
    return new NotificationMessagingTemplate(amazonSNS);
  }

  class Profiles {
    public static final String PRODUCTION = "production";
  }
}
