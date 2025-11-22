package com.web.Lixiarchos.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static JavaMailSender staticMailSender;

    @Autowired
    private JavaMailSender injectedSender;

    // Store injected sender into static field after initialization
    @PostConstruct
    private void init() {
        staticMailSender = injectedSender;
    }

    /**
     * Static method to send a simple text email.
     */
    public static void sendTextEmail(String to, String subject, String body) {
        if (staticMailSender == null) {
            throw new IllegalStateException("EmailService not initialized yet");
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        staticMailSender.send(msg);
    }

    /**
     * Static method to send an email with "from" field.
     */
    public static void sendTextEmail(String from, String to, String subject, String body) {
        if (staticMailSender == null) {
            throw new IllegalStateException("EmailService not initialized yet");
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        staticMailSender.send(msg);
    }
}
