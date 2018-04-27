package com.hedvig.memberservice.notificationService.service;

import com.hedvig.memberservice.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.memberservice.notificationService.queue.JobPoster;
import com.hedvig.memberservice.notificationService.queue.requests.SendCancellationEmailRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class NotificationService {
    private final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender mailSender;

    private final String mandateSentNotification;
    private ClassPathResource signatureImage;
    private final JobPoster jobPoster;


    public NotificationService(JavaMailSender mailSender, JobPoster jobPoster) throws IOException {
        this.mailSender = mailSender;
        this.jobPoster = jobPoster;

        mandateSentNotification = LoadEmail("notifications/insurance_mandate_sent_to_insurer.html");
        signatureImage = new ClassPathResource("mail/wordmark_mail.jpg");
    }

    public void cancellationEmailSentToInsurer(final long memberId, final CancellationEmailSentToInsurerRequest insurer)  {
        //Send email to member
        //Send push-notice to member

        jobPoster.startJob(
                new SendCancellationEmailRequest(
                    UUID.randomUUID().toString(),
                    Objects.toString(memberId),
                    insurer.getInsurer()));
    }

    private String LoadEmail(final String s) throws IOException {
        return IOUtils.toString(new ClassPathResource("mail/" + s).getInputStream(), "UTF-8");
    }
}
