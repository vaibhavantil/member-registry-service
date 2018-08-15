package com.hedvig.memberservice.notificationService.queue.jobs;

import com.hedvig.memberservice.notificationService.queue.requests.SendActivationAtFutureDateRequest;
import com.hedvig.memberservice.notificationService.serviceIntegration.expo.ExpoNotificationService;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.MemberServiceClient;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto.Member;
import io.sentry.Sentry;
import java.io.IOException;
import java.util.Objects;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class SendActivationAtFutureDateEmail {

  private Logger log = LoggerFactory.getLogger(SendActivationEmail.class);

  private final JavaMailSender mailSender;
  private final MemberServiceClient memberServiceClient;
  private final ExpoNotificationService expoNotificationService;

  private final String mandateSentNotification;
  private final ClassPathResource signatureImage;
  private static final String PUSH_MESSAGE =
      "Hej %s! Dagen är äntligen här - din Hedvigförsäkring är aktiverad! 🎉🎉!"; // TODO: CHANGE

  public SendActivationAtFutureDateEmail(
      JavaMailSender mailSender,
      MemberServiceClient memberServiceClient,
      ExpoNotificationService expoNotificationService)
      throws IOException {
    this.mailSender = mailSender;
    this.memberServiceClient = memberServiceClient;
    this.expoNotificationService = expoNotificationService;
    this.mandateSentNotification = LoadEmail("SomeEmail");
    this.signatureImage = new ClassPathResource("mail/wordmark_mail.jpg");
  }

  public void run(SendActivationAtFutureDateRequest request) {
    ResponseEntity<Member> profile = memberServiceClient.profile(request.getMemberId());

    Member body = profile.getBody();

    if (body.getEmail() != null) {
      sendEmail(body.getEmail(), body.getFirstName());
    } else {
      Sentry.capture(
          String.format("Could not find email on user with id: %s", request.getMemberId()));
    }

    sendPush(body.getMemberId(), body.getFirstName());
  }

  private void sendPush(Long memberId, String firstName) {

    String message = String.format(PUSH_MESSAGE, firstName);
    expoNotificationService.sendNotification(Objects.toString(memberId), message);
  }

  // TODO: CHANGE
  private void sendEmail(final String email, final String firstName) {
    try {
      val message = mailSender.createMimeMessage();
      val helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setSubject("Goda nyheter 🚝");
      helper.setFrom("\"Hedvig\" <hedvig@hedvig.com>");
      helper.setTo(email);

      val finalEmail = mandateSentNotification.replace("{NAME}", firstName);

      helper.setText(finalEmail, true);
      helper.addInline("image002.jpg", signatureImage);

      mailSender.send(message);
    } catch (Exception e) {
      log.error("Could not send email to member", e);
      throw new RuntimeException("Could not send email to member", e);
    }
  }

  private String LoadEmail(final String s) throws IOException {
    return IOUtils.toString(new ClassPathResource("mail/" + s).getInputStream(), "UTF-8");
  }
}
