package com.hedvig.memberservice.config;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

@Configuration
public class AWS {

    private Logger log = LoggerFactory.getLogger(AWS.class);

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSqs) {
        return new QueueMessagingTemplate(amazonSqs);
    }

}
