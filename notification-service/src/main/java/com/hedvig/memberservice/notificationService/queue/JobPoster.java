package com.hedvig.memberservice.notificationService.queue;

import com.hedvig.memberservice.notificationService.queue.jobs.SendCancellationEmail;
import com.hedvig.memberservice.notificationService.queue.requests.JobRequest;
import com.hedvig.memberservice.notificationService.queue.requests.SendCancellationEmailRequest;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;

@Component
public class JobPoster {

    private final SendCancellationEmail sendCancellationEmail;

    public JobPoster(SendCancellationEmail sendCancellationEmail) {
        this.sendCancellationEmail = sendCancellationEmail;
    }


    public void startJob(JobRequest request) throws MessagingException {

        if(SendCancellationEmailRequest.class.isInstance(request)) {
            sendCancellationEmail.run((SendCancellationEmailRequest) request);
        }
    }

}
