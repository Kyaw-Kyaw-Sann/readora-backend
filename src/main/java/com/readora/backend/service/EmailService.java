package com.readora.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String verificationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // The 'true' flag indicates this is a multipart (HTML) message
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your Readora email");

            // HTML Template matching the UI1.png design
            String htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <style>
                            body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #FAFAFA; margin: 0; padding: 40px 20px; }
                            .container { max-width: 600px; margin: 0 auto; background-color: #FFFFFF; border-radius: 12px; padding: 40px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
                            .logo { font-size: 36px; font-weight: bold; color: #CB9230; font-family: Georgia, serif; margin: 0; }
                            .tagline { color: #666666; font-size: 14px; margin-top: 5px; margin-bottom: 30px; font-style: italic; }
                            .content { color: #333333; font-size: 16px; line-height: 1.6; }
                            .btn-container { margin: 35px 0; }
                            .btn { background-color: #CB9230; color: #FFFFFF; text-decoration: none; padding: 14px 32px; border-radius: 8px; font-size: 16px; font-weight: bold; display: inline-block; }
                            .footer { margin-top: 30px; font-size: 13px; color: #999999; border-top: 1px solid #EEEEEE; padding-top: 20px; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1 class="logo">Readora</h1>
                            <p class="tagline">Stories that stay with you.</p>

                            <div class="content">
                                <p>Welcome to Readora!</p>
                                <p>We're excited to have you. Please verify your email address to unlock your personalized digital library.</p>

                                <div class="btn-container">
                                    <a href="%s" class="btn">Verify Email</a>
                                </div>

                                <p style="font-size: 14px; color: #777;">This verification link will expire soon.</p>
                            </div>

                            <div class="footer">
                                <p>If you did not create this account, you can safely ignore this email.</p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """
                    .formatted(verificationLink);

            // Set the content to HTML
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            // Throw a runtime exception or handle it via your global exception handler
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendPasswordResetOtp(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Reset your Readora password");

            String htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <style>
                            body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #FAFAFA; margin: 0; padding: 40px 20px; }
                            .container { max-width: 600px; margin: 0 auto; background-color: #FFFFFF; border-radius: 12px; padding: 40px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
                            .logo { font-size: 36px; font-weight: bold; color: #CB9230; font-family: Georgia, serif; margin: 0; }
                            .tagline { color: #666666; font-size: 14px; margin-top: 5px; margin-bottom: 30px; font-style: italic; }
                            .content { color: #333333; font-size: 16px; line-height: 1.6; }

                            /* Custom styling to make the OTP code pop */
                            .otp-box { background-color: #FDF8F0; border: 2px dashed #CB9230; color: #CB9230; font-size: 32px; font-weight: bold; letter-spacing: 8px; padding: 20px 30px; margin: 30px auto; display: inline-block; border-radius: 8px; }

                            .footer { margin-top: 30px; font-size: 13px; color: #999999; border-top: 1px solid #EEEEEE; padding-top: 20px; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1 class="logo">Readora</h1>
                            <p class="tagline">Stories that stay with you.</p>

                            <div class="content">
                                <p><strong>Password Reset Request</strong></p>
                                <p>We received a request to reset the password for your Readora account. Enter the verification code below to proceed:</p>

                                <div class="otp-box">%s</div>

                                <p style="font-size: 14px; color: #777;">This code will expire in 10 minutes.</p>
                            </div>

                            <div class="footer">
                                <p>If you did not request a password reset, you can safely ignore this email. Your password will remain unchanged.</p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """
                    .formatted(otp);

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (jakarta.mail.MessagingException e) {
            // Adjust the exception to javax.mail.MessagingException if using Spring Boot
            // 2.x
            throw new RuntimeException("Failed to send password reset OTP email", e);
        }
    }
}