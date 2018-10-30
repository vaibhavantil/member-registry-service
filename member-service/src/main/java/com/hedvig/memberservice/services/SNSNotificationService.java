package com.hedvig.memberservice.services;

import java.util.Optional;

import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SNSNotificationService {

  private final Optional<NotificationMessagingTemplate> template;

  public SNSNotificationService(Optional<NotificationMessagingTemplate> notificationMessagingTemplate) {
    this.template = notificationMessagingTemplate;
  }
	public void sendMemberSignedNotification(Long id) {
    val message = String.format("New member signed! Member has id %s", id);
    try {
      if (template.isPresent()) {
        template.get().sendNotification("newMembers", message, "CallMe");
      } else {
        log.info(String.format("[SNS NOTIFICATION] %s", message));
      }
    } catch (Exception e) {
      log.error("Could not send notification to SNS: ", e);
    }
	}

}
