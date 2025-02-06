package com.example.registerapp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class EmailUtil {

    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendOtpEmail(String email, String otp) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        try {
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Verify OTP");

            String verificationLink = buildVerificationLink(email, otp);
            String htmlContent = String.format("<div><a href=\"%s\" target=\"_blank\">Click to verify</a></div>", verificationLink);

            mimeMessageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            logger.info("OTP email sent successfully to {}", email);
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Error sending OTP email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Error sending OTP email", e);
        }
    }

    private String buildVerificationLink(String email, String otp) throws UnsupportedEncodingException {
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString());
        String encodedOtp = URLEncoder.encode(otp, StandardCharsets.UTF_8.toString());
        return String.format("http://localhost:8080/verify-account?email=%s&otp=%s", encodedEmail, encodedOtp);
    }
}
