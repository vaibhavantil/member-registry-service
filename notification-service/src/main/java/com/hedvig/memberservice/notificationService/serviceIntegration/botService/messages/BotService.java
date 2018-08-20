package com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages;

public interface BotService {
  String pushTokenId(String hid, String token);

  String getFirebasePushTokenByMemberId(String memberId, String token);
}
