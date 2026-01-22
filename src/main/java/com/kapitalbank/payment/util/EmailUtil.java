package com.kapitalbank.payment.util;

import com.kapitalbank.payment.model.dto.EmailDto;
import com.kapitalbank.payment.model.enums.LinkType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class EmailUtil {

    @Value("${user.from}")
    private String from;

    @Value("${user.password}")
    private String password;

    @Value("${user.activation.url}")
    private String activationUrl;

    @Value("${user.forget-password.url}")
    private String forgetPasswordUrl;

    @Async
    public CompletableFuture<Void> send(String from, String to, String subject, String body) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {

        }

        return CompletableFuture.completedFuture(null);
    }

    public EmailDto generateActivationEmail(String token, LinkType linkType) {
        EmailDto email = new EmailDto();
        email.setFrom(from);
        email.setSubject("Activation email");

        String link;

        String body = "";
        if (linkType == LinkType.REGISTRATION) {
            link = activationUrl + "?token=" + token;
            body = registrationEmail(link);
        } else if (linkType == LinkType.FORGET_PASSWORD) {
            link = forgetPasswordUrl + "?token=" + token;
            body = forgetPasswordEmail(link);
        } else if (linkType == LinkType.SET_PASSWORD) {
            body = setPasswordEmail(token);
        }

        email.setBody(body);

        System.out.println("activation email = " + email);

        return email;
    }

    public String registrationEmail(String link) {
        return String.format(
                "Thank you for registering on our website. To activate your profile, please follow this link: %s",
                link
        );
    }

    public String forgetPasswordEmail(String link) {
        return String.format("To reset your password, please follow this link: %s", link);
    }

    public String setPasswordEmail(String password) {
        return String.format("Your password for datarace is %s", password);
    }

}
