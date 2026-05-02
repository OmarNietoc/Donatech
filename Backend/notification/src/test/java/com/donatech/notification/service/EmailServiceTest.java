package com.donatech.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;

    @InjectMocks EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "from", "noreply@donatech.cl");
    }

    @Test
    void sendEmail_validParams_callsJavaMailSender() {
        emailService.sendEmail("recipient@test.cl", "Asunto test", "Cuerpo del mensaje");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_senderThrows_doesNotPropagateException() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        // Should not throw — service catches and logs
        emailService.sendEmail("bad@test.cl", "Asunto", "Cuerpo");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
