package com.hedvig.memberservice.eventPublishing;

import com.amazonaws.services.sns.AmazonSNS;
import com.hedvig.memberservice.events.MemberSignedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("production")
public class SNSPublisher {

    private final Logger log = LoggerFactory.getLogger(SNSPublisher.class);
    private final NotificationMessagingTemplate snsTemplate;

    @Autowired
    public SNSPublisher(AmazonSNS amazonSNS) {
        snsTemplate = new NotificationMessagingTemplate(amazonSNS);
    }

    @EventHandler
    public void on(MemberSignedEvent e) {
        try {
            snsTemplate.sendNotification("newMembers", "Ny medlem signerad!", "Ny medlem");
        }catch(Exception ex) {
            log.error("Could not send SNS-notification", ex);
        }
    }
}
