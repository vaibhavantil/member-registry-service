package com.hedvig.memberservice.notificationService.queue.jobs;

import com.hedvig.memberservice.notificationService.queue.EmailSender;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationEmailRequest;
import com.hedvig.memberservice.notificationService.serviceIntegration.expo.ExpoNotificationService;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.MemberServiceClient;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto.Member;
import java.io.IOException;
import java.util.Objects;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SendActivationEmail {

    private Logger log = LoggerFactory.getLogger(SendActivationEmail.class);

    private final EmailSender emailSender;
    private final MemberServiceClient memberServiceClient;
    private final ExpoNotificationService expoNotificationService;

    private final String mandateSentNotification;
    private final ClassPathResource signatureImage;
    private static final String PUSH_MESSAGE = "Hej %s! Dagen är äntligen här - din Hedvigförsäkring är aktiverad! 🎉🎉!";

    public SendActivationEmail(
        EmailSender emailSender,
        MemberServiceClient memberServiceClient,
        ExpoNotificationService expoNotificationService) throws IOException {
        this.emailSender = emailSender;
        this.memberServiceClient = memberServiceClient;
        this.expoNotificationService = expoNotificationService;
        this.mandateSentNotification = LoadEmail("activated.html");
        this.signatureImage = new ClassPathResource("mail/wordmark_mail.jpg");
    }


    public void run(SendActivationEmailRequest request) {
      ResponseEntity<Member> profile = memberServiceClient.profile(request.getMemberId());

      Member body = profile.getBody();

      if (body != null) {
        if (body.getEmail() != null) {
          sendEmail(request.getMemberId(), body.getEmail(), body.getFirstName());
        } else {
          log.error(
              String.format("Could not find email on user with id: %s", request.getMemberId()));
        }

        sendPush(body.getMemberId(), body.getFirstName());
      } else {
        log.error("Response body from member-service is null: {}", profile);
      }
    }

    private void sendPush(Long memberId, String firstName) {

        String message = String.format(PUSH_MESSAGE, firstName);
        expoNotificationService.sendNotification(Objects.toString(memberId), message);
    }

    private void sendEmail(final String memberId, final String email, final String firstName) {

        val finalEmail = mandateSentNotification
            .replace("{NAME}", firstName);
        emailSender.sendEmail(memberId, "Goda nyheter 🚝", email, finalEmail, signatureImage);
    }

    private String LoadEmail(final String s) throws IOException {
        return IOUtils.toString(new ClassPathResource("mail/" + s).getInputStream(), "UTF-8");
    }
}
