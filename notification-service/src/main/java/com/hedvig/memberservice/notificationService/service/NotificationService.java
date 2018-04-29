package com.hedvig.memberservice.notificationService.service;

import com.hedvig.memberservice.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.memberservice.notificationService.dto.InsuranceActivatedRequest;
import com.hedvig.memberservice.notificationService.dto.InsuranceActivationDateUpdatedRequest;
import com.hedvig.memberservice.notificationService.queue.JobPoster;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationDateUpdatedRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationEmailRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendOldInsuranceCancellationEmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class NotificationService {
    private final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JobPoster jobPoster;


    public NotificationService(JobPoster jobPoster) throws IOException {
        this.jobPoster = jobPoster;
    }

    public void cancellationEmailSentToInsurer(final long memberId, final CancellationEmailSentToInsurerRequest insurer)  {
        jobPoster.startJob(
                new SendOldInsuranceCancellationEmailRequest(
                    UUID.randomUUID().toString(),
                    Objects.toString(memberId),
                    insurer.getInsurer()));
    }

    public void insuranceActivationDateUpdated(final long memberId, final InsuranceActivationDateUpdatedRequest request) {
        jobPoster.startJob(
                new SendActivationDateUpdatedRequest(
                        UUID.randomUUID().toString(),
                        Objects.toString(memberId),
                        request.getCurrentInsurer(),
                        request.getActivationDate()));
    }

    public void insuranceActivated(final long memberId, final InsuranceActivatedRequest request) {
        jobPoster.startJob(
                new SendActivationEmailRequest(
                        UUID.randomUUID().toString(),
                        Objects.toString(memberId)));
    }
}
