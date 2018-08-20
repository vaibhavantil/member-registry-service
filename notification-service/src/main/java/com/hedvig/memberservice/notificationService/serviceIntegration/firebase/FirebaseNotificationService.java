package com.hedvig.memberservice.notificationService.serviceIntegration.firebase;

public interface FirebaseNotificationService {
  void sendNotification(String fcmToken);
}
