package com.hedvig.memberservice.notificationService.queue;

import com.hedvig.memberservice.notificationService.queue.jobs.SendActivationDateUpdatedEmail;
import com.hedvig.memberservice.notificationService.queue.jobs.SendActivationEmail;
import com.hedvig.memberservice.notificationService.queue.jobs.SendCancellationEmail;
import com.hedvig.memberservice.notificationService.queue.requests.JobRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationDateUpdatedRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationEmailRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendOldInsuranceCancellationEmailRequest;
import org.springframework.stereotype.Component;

@Component
public class JobPoster {

    private final SendCancellationEmail sendCancellationEmail;
    private final SendActivationDateUpdatedEmail sendActivationDateUpdatedEmail;
    private final SendActivationEmail sendActivationEmail;

    public JobPoster(
            SendCancellationEmail sendCancellationEmail,
            SendActivationDateUpdatedEmail sendActivationDateUpdatedEmail,
            SendActivationEmail sendActivationEmail) {
        this.sendCancellationEmail = sendCancellationEmail;
        this.sendActivationDateUpdatedEmail = sendActivationDateUpdatedEmail;
        this.sendActivationEmail = sendActivationEmail;
    }

    public void startJob(JobRequest request) {

        if(SendOldInsuranceCancellationEmailRequest.class.isInstance(request)) {
            sendCancellationEmail.run((SendOldInsuranceCancellationEmailRequest) request);
        } else if(SendActivationDateUpdatedRequest.class.isInstance(request)) {
            sendActivationDateUpdatedEmail.run((SendActivationDateUpdatedRequest)request);
        } else if(SendActivationEmailRequest.class.isInstance(request)) {
            sendActivationEmail.run((SendActivationEmailRequest) request);
        }
    }
}
