package com.hedvig.memberservice.web;


import com.hedvig.memberservice.web.dto.SendSignupRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

@RestController
@RequestMapping("/_/mail")
public class MailControler {

    @Autowired
    JavaMailSender mailSender;

    private String signupMail;

    public MailControler(JavaMailSender mailSender) throws IOException {
        this.mailSender = mailSender;
        ClassPathResource resource = new ClassPathResource("mail/waitlist.html");
        signupMail = IOUtils.toString(resource.getInputStream(), "UTF-8");
    }

    @RequestMapping("/sendSignup")
    public String sendMail(@RequestBody SendSignupRequest request) throws MessagingException, IOException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setSubject("Välkommen till hedvig!");
        helper.setFrom("\"Hedvig\" <hedvig@hedvig.com>");

        helper.setTo(request.email);


        helper.setText(signupMail.replace("{TOKEN}", request.token.toString()), true);
        this.mailSender.send(message);

        return "";
    }
}
