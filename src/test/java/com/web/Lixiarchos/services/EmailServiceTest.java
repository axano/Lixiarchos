package com.web.Lixiarchos.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() throws Exception {
        mailSender = mock(JavaMailSender.class);
        setStaticMailSender(mailSender);
    }

    @AfterEach
    void tearDown() throws Exception {
        setStaticMailSender(null);
    }

    private void setStaticMailSender(JavaMailSender sender) throws Exception {
        Field field = EmailService.class.getDeclaredField("staticMailSender");
        field.setAccessible(true);
        field.set(null, sender);
    }

    @Test
    void sendTextEmail_3Args_sendsCorrectMessage() {
        EmailService.sendTextEmail("to@example.com", "Subject", "Body text");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertArrayEquals(new String[]{"to@example.com"}, msg.getTo());
        assertEquals("Subject", msg.getSubject());
        assertEquals("Body text", msg.getText());
    }

    @Test
    void sendTextEmail_4Args_sendsCorrectMessageWithFrom() {
        EmailService.sendTextEmail("from@example.com", "to@example.com", "Subject", "Body");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertEquals("from@example.com", msg.getFrom());
        assertArrayEquals(new String[]{"to@example.com"}, msg.getTo());
        assertEquals("Subject", msg.getSubject());
        assertEquals("Body", msg.getText());
    }

    @Test
    void sendTextEmail_3Args_whenNotInitialized_throwsIllegalStateException() throws Exception {
        setStaticMailSender(null);

        assertThrows(IllegalStateException.class,
                () -> EmailService.sendTextEmail("to@example.com", "Subject", "Body"));
    }

    @Test
    void sendTextEmail_4Args_whenNotInitialized_throwsIllegalStateException() throws Exception {
        setStaticMailSender(null);

        assertThrows(IllegalStateException.class,
                () -> EmailService.sendTextEmail("from@example.com", "to@example.com", "Subject", "Body"));
    }
}
