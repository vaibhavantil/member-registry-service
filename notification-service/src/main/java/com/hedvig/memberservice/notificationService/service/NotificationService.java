package com.hedvig.memberservice.notificationService.service;

import com.hedvig.memberservice.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.memberservice.notificationService.dto.InsuranceActivationDateUpdatedRequest;
import com.hedvig.memberservice.notificationService.queue.JobPoster;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationAtFutureDateRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationDateUpdatedRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationEmailRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendOldInsuranceCancellationEmailRequest;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
  private final Logger log = LoggerFactory.getLogger(NotificationService.class);

  private final JobPoster jobPoster;

  public NotificationService(JobPoster jobPoster) throws IOException {
    this.jobPoster = jobPoster;
  }

  public void cancellationEmailSentToInsurer(
      final long memberId, final CancellationEmailSentToInsurerRequest insurer) {
    SendOldInsuranceCancellationEmailRequest request =
        new SendOldInsuranceCancellationEmailRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setMemberId(Objects.toString(memberId));
    request.setInsurer(insurer.getInsurer());

    jobPoster.startJob(request, true);
  }

  public void insuranceActivationDateUpdated(
      final long memberId, final InsuranceActivationDateUpdatedRequest request) {
    SendActivationDateUpdatedRequest request2 = new SendActivationDateUpdatedRequest();
    request2.setRequestId(UUID.randomUUID().toString());
    request2.setMemberId(Objects.toString(memberId));
    request2.setInsurer(request.getCurrentInsurer());
    request2.setActivationDate(request.getActivationDate());
    jobPoster.startJob(request2, false);
  }

  public void insuranceActivated(final long memberId) {
    SendActivationEmailRequest request = new SendActivationEmailRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setMemberId(Objects.toString(memberId));
    jobPoster.startJob(request, false);
  }

  public void insuranceActivationAtFutureDate (final long memberId, final String activationDate) {
    SendActivationAtFutureDateRequest request = new SendActivationAtFutureDateRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setMemberId(Objects.toString(memberId));
    request.setActivationDate(activationDate);
    jobPoster.startJob(request, false);
  }

  public void sendPushNotification() {

  }

}
