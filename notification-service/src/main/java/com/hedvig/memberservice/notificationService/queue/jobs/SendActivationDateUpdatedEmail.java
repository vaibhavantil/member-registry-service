package com.hedvig.memberservice.notificationService.queue.jobs;

import com.hedvig.memberservice.notificationService.queue.EmailSender;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationDateUpdatedRequest;
import com.hedvig.memberservice.notificationService.serviceIntegration.expo.ExpoNotificationService;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.MemberServiceClient;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto.Member;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class SendActivationDateUpdatedEmail {

    private final Logger log = LoggerFactory.getLogger(SendActivationDateUpdatedEmail.class);

  private final EmailSender emailSender;

  private final MemberServiceClient memberServiceClient;
    private final ExpoNotificationService expoNotificationService;

    private final String mandateSentNotification;
    private final ClassPathResource signatureImage;
    private static final String PUSH_MESSAGE = "Hej %s! Bra nyheter! %s har bekräftat ditt uppsägningsdatum - det är %s. Samma dag aktiveras din Hedvigförsäkring. Jag hör av mig då! 🙌";

    public SendActivationDateUpdatedEmail(
        EmailSender emailSender,
        MemberServiceClient memberServiceClient,
        ExpoNotificationService expoNotificationService)
            throws IOException {
      this.emailSender = emailSender;
      this.memberServiceClient = memberServiceClient;
      this.expoNotificationService = expoNotificationService;

      mandateSentNotification = LoadEmail("notifications/insurance_activation_date_updated.html");
      signatureImage = new ClassPathResource("mail/wordmark_mail.jpg");
    }


    public void run(SendActivationDateUpdatedRequest request) {

      ResponseEntity<Member> profile = memberServiceClient.profile(request.getMemberId());
      Member body = profile.getBody();

      if (body != null) {

        val format = DateTimeFormatter.ofPattern("d MMMM yyyy").withLocale(new Locale("sv", "SE"));
        val localDate = request.getActivationDate().atZone(ZoneId.of("Europe/Stockholm"));

        if (body.getEmail() != null) {
          sendEmail(
              request.getMemberId(),
              body.getEmail(),
              body.getFirstName(),
              request.getInsurer(),
              localDate.format(format)
          );
        } else {
          log.error(
              String.format("Could not find email on user with id: %s", request.getMemberId()));
        }

        sendPush(body.getMemberId(), body.getFirstName(), request.getInsurer(),
            localDate.format(format));
      }
      else{
        log.error("Response body from member-service is null: {}", profile);
      }
    }

    private void sendPush(Long memberId, String firstName, String insurer, String date) {

        String message = String.format(PUSH_MESSAGE, firstName, insurer, date);
        expoNotificationService.sendNotification(Objects.toString(memberId), message);
    }

  private void sendEmail(
      final String memberId,
      final String email,
      final String firstName,
      final String insurer,
      final String date) {
    val finalEmail = mandateSentNotification
        .replace("{FIRST_NAME}", firstName)
        .replace("{INSURER}", insurer)
        .replace("{DATE}", date);

    emailSender.sendEmail(memberId, "Goda nyheter 🚝", email, finalEmail, signatureImage);
    }

    private String LoadEmail(final String s) throws IOException {
        return IOUtils.toString(new ClassPathResource("mail/" + s).getInputStream(), "UTF-8");
    }
}
