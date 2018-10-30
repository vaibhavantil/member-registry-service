package com.hedvig.memberservice.services;

import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SNSNotificationService {

  private final NotificationMessagingTemplate template;

  public SNSNotificationService(NotificationMessagingTemplate notificationMessagingTemplate) {
    this.template = notificationMessagingTemplate;
  }
	public void sendMemberSignedNotification(Long id) {
    template.sendNotification("newMembers", String.format("New member signed! Member has id %s", id), "CallMe");
	}

}
