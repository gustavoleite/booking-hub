package com.bookinghub.notification.infrastructure.adapters.out.email;

import com.bookinghub.notification.core.ports.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JavaMailEmailAdapter implements EmailPort {

    private final JavaMailSender mailSender;

    @Override
    public void send(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn("Skipping email — recipient is null or blank. Subject: {}", subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} — subject: {}", to, subject);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            }
        }
    }
}
