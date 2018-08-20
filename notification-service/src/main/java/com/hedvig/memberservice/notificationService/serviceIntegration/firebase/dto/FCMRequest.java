package com.hedvig.memberservice.notificationService.serviceIntegration.firebase.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.val;

public final class FCMRequest {
  Map<String, Object> message;

  public FCMRequest(String token, String body, String title) {
    val notification = new HashMap<String, Object>();
    notification.put("body", body);
    notification.put("title", title);

    this.message = new HashMap<>();
    message.put("token", token);
    message.put("notification", notification);
  }
}
