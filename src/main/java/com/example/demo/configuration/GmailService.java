package com.example.demo.configuration;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@Service
public class GmailService implements EmailService{

    private static final Logger LOGGER = LoggerFactory.getLogger(GmailService.class);

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendEmail(String to, String subject, String message) {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        try {
            helper.setFrom("arixpresslogistics@gmail.com", "AriXpress Logistics");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, true);

            mailSender.send(mimeMessage);
        }
        catch (MessagingException | UnsupportedEncodingException e) {
            LOGGER.error("An error occurred while sending an email to address : "
                    + to + "; error: " + e.getMessage());
        }
    }
}
