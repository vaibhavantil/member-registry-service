package com.hedvig.integration.notificationService;

import com.hedvig.integration.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.integration.notificationService.dto.InsuranceActivationDateUpdatedRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

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

  @DeleteMapping("/_/customerio/{memberId}")
  ResponseEntity<?> deleteCustomer(@PathVariable @NotNull String memberId);

  @PostMapping("/_/customerio/{memberId}")
  ResponseEntity<?> updateCustomer(@PathVariable @NotNull String memberId, @RequestBody @NotNull Map<String, Object> data);
}
