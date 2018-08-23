package com.hedvig.memberservice.externalApi.notificationService;

import com.hedvig.memberservice.externalApi.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.memberservice.externalApi.notificationService.dto.InsuranceActivationDateUpdatedRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "notification-service",
    url = "${hedvig.notificationservice.baseurl:notification-service}")
public interface NotificationServiceClient {

  @PostMapping("/_/notifications/{memberId}/cancellationEmailSentToInsurer")
  ResponseEntity<?> cancellationEmailSentToInsurer(
      @PathVariable(name = "memberId") Long memberId, @RequestBody CancellationEmailSentToInsurerRequest body);

  @PostMapping("/_/notifications/{memberId}/insuranceActivated")
  ResponseEntity<?> insuranceActivated(@PathVariable(name = "memberId") Long memberId);

  @PostMapping("/_/notifications/{memberId}/insuranceActivationDateUpdated")
  ResponseEntity<?> insuranceActivationDateUpdated(
      @PathVariable(name = "memberId") Long memberId, @RequestBody InsuranceActivationDateUpdatedRequest body);

  @PostMapping("/_/notifications/insuranceWillBeActivatedAt")
  ResponseEntity<?> insuranceReminder(@RequestBody int NumberOfDaysFromToday);
}
