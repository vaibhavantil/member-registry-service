package com.hedvig.memberservice.notificationService.queue.jobs;

import com.hedvig.memberservice.notificationService.queue.requests.SendCancellationEmailRequest;
import com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages.BotService;
import com.hedvig.memberservice.notificationService.serviceIntegration.expo.ExpoNotificationService;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.MemberServiceClient;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto.Member;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Objects;

@Component
public class SendCancellationEmail {

    private final Logger log = LoggerFactory.getLogger(SendCancellationEmail.class);

    private final JavaMailSender mailSender;
    private final MemberServiceClient memberServiceClient;
    private final BotService botService;
    private final ExpoNotificationService expoNotificationService;

    private final String mandateSentNotification;
    private ClassPathResource signatureImage;

    public SendCancellationEmail(JavaMailSender mailSender, MemberServiceClient memberServiceClient, BotService botService, ExpoNotificationService expoNotificationService) throws IOException {
        this.mailSender = mailSender;
        this.memberServiceClient = memberServiceClient;
        this.botService = botService;
        this.expoNotificationService = expoNotificationService;


        mandateSentNotification = LoadEmail("notifications/insurance_mandate_sent_to_insurer.html");
        signatureImage = new ClassPathResource("mail/wordmark_mail.jpg");
    }


    public void run(SendCancellationEmailRequest request) throws MessagingException {

            ResponseEntity<Member> profile = memberServiceClient.profile(request.getMemberId());

            Member body = profile.getBody();

            sendEmail(body.getEmail(), body.getFirstName(), request.getInsurer());
            sendPush(body.getMemberId(), body.getFirstName(), request.getInsurer());
    }

    private void sendPush(Long memberId, String firstName, String insurer) {

        String message = String.format("Hej %s! Tänkte bara meddela att vi har skickat en uppsägning till ditt gamla försäkringsbolag %s nu! Jag återkommer till dig när de har bekräftat ditt uppsägningsdatum. Hej så länge! 👋", firstName, insurer);
        expoNotificationService.sendNotification(Objects.toString(memberId), message);

    }

    private void sendEmail(final String email, final String firstName, final String insurer)
            throws MessagingException {
        try {
            val message = mailSender.createMimeMessage();
            val helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setSubject("Flytten har startat 🚝");
            helper.setFrom("\"Hedvig\" <hedvig@hedvig.com>");
            helper.setTo(email);

            val finalEmail = mandateSentNotification
                    .replace("{FIRST_NAME}", firstName)
                    .replace("{INSURER}", insurer);

            helper.setText(finalEmail, true);
            helper.addInline("image002.jpg", signatureImage);

            mailSender.send(message);
        }catch (Exception e) {
            log.error("Could not send email to member", e);
            throw new RuntimeException("Could not send email to member", e);
        }
    }

    private String LoadEmail(final String s) throws IOException {
        return IOUtils.toString(new ClassPathResource("mail/" + s).getInputStream(), "UTF-8");
    }
}
