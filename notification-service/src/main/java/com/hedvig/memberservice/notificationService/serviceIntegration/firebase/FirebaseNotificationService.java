package com.hedvig.memberservice.notificationService.serviceIntegration.firebase;

public interface FirebaseNotificationService {
  void sendNewMessageNotification(String fcmToken);

  void sendNotification(String memberId, String message);
}
