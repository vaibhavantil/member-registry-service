package com.hedvig.memberservice.web;


import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import com.hedvig.memberservice.web.dto.SendActivatedRequest;
import com.hedvig.memberservice.web.dto.SendOnboardedActiveLaterRequest;
import com.hedvig.memberservice.web.dto.SendOnboardedActiveTodayRequest;
import com.hedvig.memberservice.web.dto.SendSignupRequest;
import com.hedvig.memberservice.web.dto.SendWaitIsOverRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.val;

import java.io.IOException;

@RestController
@RequestMapping("/_/mail")
public class MailController {

    @Autowired
    JavaMailSender mailSender;
    private final String webSiteBaseUrl;

    private String signupMail;
    private String onboardedTodayMail;
    private String onboardedLaterMail;
    private String activatedMail;
    private String waitIsOverMail;
    private ClassPathResource signatureImage;
    private ProductApi productApi;

    public MailController(JavaMailSender mailSender, @Value("${hedvig.websiteBaseUrl:https://www.hedvig.com}") String webSiteBaseUrl, ProductApi productApi) throws IOException {
        this.mailSender = mailSender;
        this.webSiteBaseUrl = webSiteBaseUrl;
        this.productApi = productApi;
        ClassPathResource signupMailResource = new ClassPathResource("mail/waitlist.html");
        val onboardedTodayMailResource = new ClassPathResource("mail/onboarded_today.html");
        val onboardedLaterMailResource = new ClassPathResource("mail/onboarded_later.html");
        val activatedMailResource = new ClassPathResource("mail/activated.html");
        val waitIsOverResource = new ClassPathResource("mail/wait_is_over.html");
        signatureImage = new ClassPathResource("mail/wordmark_mail.jpg");
        signupMail = IOUtils.toString(signupMailResource.getInputStream(), "UTF-8");
        onboardedTodayMail = IOUtils.toString(onboardedTodayMailResource.getInputStream(), "UTF-8");
        onboardedLaterMail = IOUtils.toString(onboardedLaterMailResource.getInputStream(), "UTF-8");
        activatedMail = IOUtils.toString(activatedMailResource.getInputStream(), "UTF-8");
        waitIsOverMail = IOUtils.toString(waitIsOverResource.getInputStream(), "UTF-8");
    }

    @RequestMapping("/sendSignup")
    public String sendMail(@RequestBody SendSignupRequest request) throws MessagingException, IOException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setSubject("Snart är det dags!");
        helper.setFrom("\"Hedvig\" <hedvig@hedvig.com>");

        helper.setTo(request.email);


        helper.setText(signupMail.replace("{TOKEN}", request.token.toString()).replace("{BASE_URL}", webSiteBaseUrl), true);
        this.mailSender.send(message);

        return "";
    }

    @RequestMapping(value = "/sendOnboardedActiveToday", method = RequestMethod.POST)
    public String sendOnboardedActiveTodayMail(@RequestBody SendOnboardedActiveTodayRequest request) throws MessagingException {
        val message = mailSender.createMimeMessage();
        val helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setSubject("Välkommen till Hedvig! 🙌");
        helper.setFrom("\"Hedvig\" <hedvig@hedvig.com>");
        helper.setTo(request.getEmail());

        val templatedMail = onboardedTodayMail
            .replace("{NAME}", request.getName());
        helper.setText(templatedMail, true);
        helper.addInline("image002.jpg", signatureImage);

        mailSender.send(message);

        return "";
    }

    @RequestMapping(value = "/sendOnboardedActiveLater", method = RequestMethod.POST)
    public String sendOnboardedActiveLaterMail(@RequestBody SendOnboardedActiveLaterRequest request) throws MessagingException {
        val message = mailSender.createMimeMessage();
        val helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setSubject("Välkommen till Hedvig! 🙌");
        helper.setFrom("\"Hedvig\" <hedvig@hedvig.com>");
        helper.setTo(request.getEmail());
        val pdf = productApi.getContract(request.memberId);

        val templatedMail = onboardedLaterMail
            .replace("{NAME}", request.getName());

        helper.setText(templatedMail, true);
        helper.addAttachment("fullmakt.pdf", new ByteArrayResource(pdf));
        helper.addInline("image002.jpg", signatureImage);

        mailSender.send(message);
        return "";
    }

    @RequestMapping(value = "/sendActivated", method = RequestMethod.POST)
    public String sendActivated(@RequestBody SendActivatedRequest request) throws MessagingException {
        val message = mailSender.createMimeMessage();
        val helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setSubject("Din försäkring har aktiverats ✔️");
        helper.setFrom("\"Hedvig\" <hedvig@hedvig.com>");
        helper.setTo(request.getEmail());
        val templatedMail = activatedMail
            .replace("{NAME}", request.getName());
        helper.setText(templatedMail, true);
        helper.addInline("image002.jpg", signatureImage);

        mailSender.send(message);
        return "";
    }

    @RequestMapping(value = "/sendWaitIsOver", method = RequestMethod.POST)
    public String sendWaitIsOverMail(@RequestBody SendWaitIsOverRequest request) throws MessagingException {
        val message = mailSender.createMimeMessage();
        val helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setSubject("Väntan är över - nu kan du aktivera din försäkring ⚡️");
        helper.setFrom("\"Hedvig\" <hedvig@hedvig.com>");
        helper.setTo(request.getEmail());

        helper.setText(waitIsOverMail, true);
        helper.addInline("image002.jpg", signatureImage);

        mailSender.send(message);
        return "";
    }
}
