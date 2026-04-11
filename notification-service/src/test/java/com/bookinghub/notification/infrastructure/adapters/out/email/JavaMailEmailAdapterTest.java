package com.bookinghub.notification.infrastructure.adapters.out.email;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class JavaMailEmailAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    private JavaMailEmailAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JavaMailEmailAdapter(mailSender);
    }

    @Test
    void shouldSendEmail_whenRecipientIsValid() {
        adapter.send("user@example.com", "Assunto", "Corpo do email");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldSkip_whenRecipientIsNull() {
        adapter.send(null, "Assunto", "Corpo");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldSkip_whenRecipientIsBlank() {
        adapter.send("   ", "Assunto", "Corpo");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldNotThrow_whenMailSenderFails() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        adapter.send("user@example.com", "Assunto", "Corpo");
        // exception must be swallowed — no assertion needed beyond not throwing
    }
}
