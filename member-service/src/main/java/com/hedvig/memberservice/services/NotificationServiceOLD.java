package com.hedvig.memberservice.services;

import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.web.dto.CancellationEmailSentToInsurerRequest;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Optional;

@Service
public class NotificationService {
    private final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender mailSender;
    private final MemberRepository repository;

    private final String mandateSentNotification;
    private ClassPathResource signatureImage;


    public NotificationService(JavaMailSender mailSender, MemberRepository repository) throws IOException {
        this.mailSender = mailSender;
        this.repository = repository;

        mandateSentNotification = LoadEmail("notifications/insuranceMandateSentToInsurer.html");
        signatureImage = new ClassPathResource("mail/wordmark_mail.jpg");
    }

    public void cancellationEmailSentToInsurer(final long memberId, final CancellationEmailSentToInsurerRequest insurer)  {
        //Send email to member
        //Send push-notice to member

        Optional<MemberEntity> byId = repository.findById(memberId);

        byId.ifPresent(member -> {
            try {
                val message = mailSender.createMimeMessage();
                val helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setSubject("Flytten har startat 🚝");
                helper.setFrom("\"Hedvig\" <hedvig@hedvig.com>");
                helper.setTo(member.getEmail());

                val finalEmail = mandateSentNotification
                        .replace("{FIRST_NAME}", member.getFirstName())
                        .replace("{INSURER}", insurer.getInsurer());

                helper.setText(finalEmail, true);
                helper.addInline("image002.jpg", signatureImage);

                mailSender.send(message);
            }catch (Exception e) {
                log.error("Could not send email to member", e);
            }
        });
    }

    private String LoadEmail(final String s) throws IOException {
        return IOUtils.toString(new ClassPathResource("mail/" + s).getInputStream(), "UTF-8");
    }
}
