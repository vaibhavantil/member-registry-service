package com.hedvig.memberservice.notificationService.queue.jobs;

import com.hedvig.memberservice.notificationService.queue.EmailSender;
import com.hedvig.memberservice.notificationService.queue.requests.SendOldInsuranceCancellationEmailRequest;
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
import org.springframework.stereotype.Component;

@Component
public class SendCancellationEmail {

    private final Logger log = LoggerFactory.getLogger(SendCancellationEmail.class);

    private final EmailSender emailSender;
  private final MemberServiceClient memberServiceClient;
    private final ExpoNotificationService expoNotificationService;

    private final String mandateSentNotification;
    private final ClassPathResource signatureImage;
    private static final String PUSH_MESSAGE = "Hej %s! Tänkte bara meddela att vi har skickat en uppsägning till ditt gamla försäkringsbolag %s nu! Jag återkommer till dig när de har bekräftat ditt uppsägningsdatum. Hej så länge! 👋";

    public SendCancellationEmail(
        EmailSender emailSender,
        MemberServiceClient memberServiceClient,
        ExpoNotificationService expoNotificationService) throws IOException {
        this.emailSender = emailSender;
      this.memberServiceClient = memberServiceClient;
        this.expoNotificationService = expoNotificationService;


        mandateSentNotification = LoadEmail("notifications/insurance_mandate_sent_to_insurer.html");
        signatureImage = new ClassPathResource("mail/wordmark_mail.jpg");
    }


    public void run(SendOldInsuranceCancellationEmailRequest request) {
      ResponseEntity<Member> profile = memberServiceClient.profile(request.getMemberId());
      Member body = profile.getBody();

      if (body != null) {
        if (body.getEmail() != null) {
          sendEmail(body.getEmail(), body.getFirstName(), request.getInsurer());
        } else {
          log.error(
              String.format("Could not find email on user with id: %s", request.getMemberId()));
        }

        sendPush(body.getMemberId(), body.getFirstName(), request.getInsurer());

      } else {
        log.error("Response body from member-service is null: {}", profile);
      }
    }

    private void sendPush(Long memberId, String firstName, String insurer) {

        String message = String.format(PUSH_MESSAGE, firstName, insurer);
        expoNotificationService.sendNotification(Objects.toString(memberId), message);

    }

    private void sendEmail(final String email, final String firstName, final String insurer) {

        val finalEmail = mandateSentNotification
            .replace("{FIRST_NAME}", firstName)
            .replace("{INSURER}", insurer);

        emailSender.sendEmail("", "Flytten har startat 🚝", email, finalEmail, signatureImage);
    }

    private String LoadEmail(final String s) throws IOException {
        return IOUtils.toString(new ClassPathResource("mail/" + s).getInputStream(), "UTF-8");
    }
}
