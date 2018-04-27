package com.hedvig.memberservice.notificationService.serviceIntegration.expo;

public interface ExpoNotificationService {
    void sendNotification(String hid, String message);
}