package com.hedvig.memberservice.notificationService.serviceIntegration.firebase;

import com.hedvig.memberservice.notificationService.serviceIntegration.firebase.dto.FCMRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient
public interface FirebaseClient {
  void sendPush(FCMRequest request, @RequestHeader("authorization") String authorization);
}
