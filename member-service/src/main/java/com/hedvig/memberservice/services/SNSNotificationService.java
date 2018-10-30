package com.hedvig.memberservice.services;

import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SNSNotificationService {

  private final NotificationMessagingTemplate template;

  public SNSNotificationService(NotificationMessagingTemplate notificationMessagingTemplate) {
    this.template = notificationMessagingTemplate;
  }
	public void sendMemberSignedNotification(Long id) {
    try {
      template.sendNotification("newMembers", String.format("New member signed! Member has id %s", id), "CallMe");
    } catch (Exception e) {
      log.error("Could not send notification to SNS: ", e);
    }
	}

}
