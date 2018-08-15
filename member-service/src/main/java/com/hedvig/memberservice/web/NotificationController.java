package com.hedvig.memberservice.web;

import com.hedvig.memberservice.externalApi.productsPricing.ProductClient;
import com.hedvig.memberservice.externalApi.productsPricing.dto.InsuranceNotificationDTO;
import com.hedvig.memberservice.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.memberservice.notificationService.dto.InsuranceActivatedRequest;
import com.hedvig.memberservice.notificationService.dto.InsuranceActivationDateUpdatedRequest;
import com.hedvig.memberservice.notificationService.service.NotificationService;
import feign.FeignException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
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
  private final ProductClient productPricingService;

  public NotificationController(
      NotificationService notificationService, ProductClient productPricingService) {
    this.notificationService = notificationService;
    this.productPricingService = productPricingService;
  }

  @PostMapping("cancellationEmailSentToInsurer")
  public ResponseEntity<?> cancellationEmailSentToInsurer(
      @PathVariable Long memberId, @RequestBody CancellationEmailSentToInsurerRequest body) {
    MDC.put("memberId", Objects.toString(memberId));
    try {
      notificationService.cancellationEmailSentToInsurer(memberId, body);
    } catch (MailException e) {
      log.error("Could not send email to member", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.noContent().build();
  }

  @PostMapping("insuranceActivated")
  public ResponseEntity<?> insuranceActivated(
      @PathVariable Long memberId, @RequestBody InsuranceActivatedRequest body) {
    MDC.put("memberId", Objects.toString(memberId));
    try {
      notificationService.insuranceActivated(memberId);
    } catch (MailException e) {
      log.error("Could not send email to member", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.noContent().build();
  }

  @PostMapping("insuranceActivationDateUpdated")
  public ResponseEntity<?> insuranceActivationDateUpdated(
      @PathVariable Long memberId, @RequestBody InsuranceActivationDateUpdatedRequest body) {
    MDC.put("memberId", Objects.toString(memberId));
    try {
      notificationService.insuranceActivationDateUpdated(memberId, body);
    } catch (MailException e) {
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

    if (NumberOfDaysFromToday < 0) {
      return ResponseEntity.badRequest().build();
    }

    try {
      ResponseEntity<List<InsuranceNotificationDTO>> insuranceResponse =
          productPricingService.getInsurancesByActivationDate(
              LocalDate.now().plusDays(NumberOfDaysFromToday));

      final List<InsuranceNotificationDTO> insurancesToRemind = insuranceResponse.getBody();

      if (insurancesToRemind != null && insurancesToRemind.size() > 0) {
        try {
          if (NumberOfDaysFromToday == 0) {
            insurancesToRemind.forEach(
                i -> notificationService.insuranceActivated(Long.parseLong(i.getMemberId())));
          } else {
            insurancesToRemind.forEach(
                i ->
                    notificationService.insuranceActivationAtFutureDate(
                        Long.parseLong(i.getMemberId()), i.getActivationDate().toString()));
          }
        } catch (MailException e) {
          log.error("Could not send email to member", e);
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (FeignException ex) {
      if (ex.status() != 404) {
        log.error("Error from products-pricing", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
      return ResponseEntity.notFound().build();
    }
  }
}
