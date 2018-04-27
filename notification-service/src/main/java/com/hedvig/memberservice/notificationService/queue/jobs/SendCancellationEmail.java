package com.hedvig.memberservice.notificationService.queue.jobs;

import com.hedvig.memberservice.notificationService.queue.requests.SendCancellationEmailRequest;
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

import java.io.IOException;

@Component
public class SendCancellationEmail {

    private final Logger log = LoggerFactory.getLogger(SendCancellationEmail.class);

    private final JavaMailSender mailSender;
    private final MemberServiceClient memberServiceClient;

    private final String mandateSentNotification;
    private ClassPathResource signatureImage;

    public SendCancellationEmail(JavaMailSender mailSender, MemberServiceClient memberServiceClient) throws IOException {
        this.mailSender = mailSender;
        this.memberServiceClient = memberServiceClient;


        mandateSentNotification = LoadEmail("notifications/insurance_mandate_sent_to_insurer.html");
        signatureImage = new ClassPathResource("mail/wordmark_mail.jpg");
    }


    public void run(SendCancellationEmailRequest request) {
        try {

            ResponseEntity<Member> profile = memberServiceClient.profile(request.getMemberId());

            Member body = profile.getBody();

            val message = mailSender.createMimeMessage();
            val helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setSubject("Flytten har startat 🚝");
            helper.setFrom("\"Hedvig\" <hedvig@hedvig.com>");
            helper.setTo(body.getEmail());

            val finalEmail = mandateSentNotification
                    .replace("{FIRST_NAME}", body.getFirstName())
                    .replace("{INSURER}", request.getInsurer());

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
