package com.hedvig.memberservice.web;

import com.hedvig.memberservice.externalApi.notificationService.NotificationService;
import com.hedvig.memberservice.externalApi.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.memberservice.externalApi.notificationService.dto.InsuranceActivationDateUpdatedRequest;
import java.util.Objects;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_/member/{memberId}/notifications")
public class NotificationController {

  private final Logger log = LoggerFactory.getLogger(NotificationController.class);
  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @PostMapping("cancellationEmailSentToInsurer")
  public ResponseEntity<?> cancellationEmailSentToInsurer(
      @PathVariable Long memberId, @RequestBody CancellationEmailSentToInsurerRequest body) {
    MDC.put("memberId", Objects.toString(memberId));
    try {
      notificationService.cancellationEmailSentToInsurer(memberId, body);
    } catch (Exception e) {
      log.error("Could not send email to member", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.noContent().build();
  }

  @PostMapping("insuranceActivated")
  public ResponseEntity<?> insuranceActivated(@PathVariable Long memberId) {
    MDC.put("memberId", Objects.toString(memberId));
    try {
      notificationService.insuranceActivated(memberId);
    } catch (Exception e) {
      log.error("Could not send email to member", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.noContent().build();
  }

  @PostMapping("insuranceActivationDateUpdated")
  public ResponseEntity<?> insuranceActivationDateUpdated(
      @PathVariable Long memberId, @RequestBody @Valid InsuranceActivationDateUpdatedRequest body) {
    MDC.put("memberId", Objects.toString(memberId));
    try {
      notificationService.insuranceActivationDateUpdated(memberId, body);
    } catch (Exception e) {
      log.error("Could not send email to member", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.noContent().build();
  }

  /**
   * This endpoint is called x days before the activation, in order to notify members for their
   * insurance's activation. @RequestBody NumberOfDaysFromToday the numbers from today that the
   * insurance will be activated
   *
   * @return 204 on success
   *     <p>or
   * @return 404 not found if there is no isnurance that will be activated on that date
   */
  @PostMapping("insuranceWillBeActivatedAt")
  public ResponseEntity<?> insuranceReminder(@RequestBody int NumberOfDaysFromToday) {
    try {
      notificationService.insuranceReminder(NumberOfDaysFromToday);
    } catch (Exception e) {
      log.error("Could not send emails ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    return ResponseEntity.noContent().build();
  }
}
