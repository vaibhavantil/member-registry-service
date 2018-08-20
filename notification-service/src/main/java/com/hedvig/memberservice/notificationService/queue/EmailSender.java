package com.hedvig.memberservice.notificationService.queue;

import java.util.Arrays;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {

  private final Logger log = LoggerFactory.getLogger(EmailSender.class);
  private final JavaMailSender mailSender;
  private final String[] bcc;

  public EmailSender(
      JavaMailSender mailSender, @Value("${hedvig.notificationService.memberBcc}") String[] bcc) {
    this.mailSender = mailSender;
    this.bcc = bcc;
  }

  public void sendEmail(
      final String memberId,
      final String subject,
      final String email,
      final String html,
      final ClassPathResource signatureImage) {
    try {
      val message = mailSender.createMimeMessage();
      val helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setSubject(subject);
      helper.setFrom("\"Hedvig\" <hedvig@hedvig.com>");
      helper.setTo(email);
      helper.setBcc(
          Arrays.stream(bcc)
              .map(
                  x -> new MemberBCCAddress(x).format(memberId))
              .toArray(String[]::new));

      helper.setText(html, true);
      if (signatureImage != null) {
        helper.addInline("image002.jpg", signatureImage);
      }

      mailSender.send(message);
    } catch (Exception e) {
      log.error("Could not send email to member", e);
      throw new RuntimeException("Could not send email to member", e);
    }
  }
}
