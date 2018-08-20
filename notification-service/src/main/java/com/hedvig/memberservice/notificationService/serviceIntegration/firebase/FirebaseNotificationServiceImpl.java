package com.hedvig.memberservice.notificationService.serviceIntegration.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages.BotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirebaseNotificationServiceImpl implements FirebaseNotificationService {
  public static final String TITLE = "Hedvig";
  public static final String BODY = "Hej, du har ett nytt meddelande från Hedvig!";

  private final BotService botService;

  private static Logger logger = LoggerFactory.getLogger(FirebaseNotificationServiceImpl.class);

  public FirebaseNotificationServiceImpl(BotService botService) {
    this.botService = botService;
  }

  @Override
  public void sendNewMessageNotification(String token) {
    Message message =
        Message.builder().putData("title", TITLE).putData("body", BODY).setToken(token).build();

    try {
      String response = FirebaseMessaging.getInstance().send(message);

      logger.info("Response from pushing notification {}", response);
    } catch (FirebaseMessagingException e) {
      logger.error(
          "SendNewMessageNotification: Cannot send notification with token {} through firebase. Error: {}",
          token,
          e);
    }
  }

  @Override
  public void sendNotification(String memberId, String body) {

    String memberToken = botService.getFirebasePushTokenByMemberId(memberId, "");

    Message message =
        Message.builder()
            .putData("title", TITLE)
            .putData("body", body)
            .setToken(memberToken)
            .build();
    try {
      String response = FirebaseMessaging.getInstance().send(message);

      logger.info("Response from pushing notification {}", response);
    } catch (FirebaseMessagingException e) {
      logger.error(
          "SendNewMessageNotification: Cannot send notification with memberId {} through firebase. Error: {}",
          memberId,
          e);
    }
  }
}
