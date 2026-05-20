package com.ibfarms.service;

import com.ibfarms.config.IbFarmsProperties;
import com.ibfarms.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final IbFarmsProperties properties;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public void sendPendingApprovalToAdmin(User user, String approvalUrl) {
        String body = """
                A new user registered on IB Farms and is waiting for your approval.

                Username: %s
                Full name: %s
                Email: %s
                Registered: %s

                Approve this account (one-time link):
                %s

                If you did not expect this registration, ignore this email.
                """.formatted(
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getCreatedAt(),
                approvalUrl);

        send(properties.getAdminEmail(), "IB Farms – Approve new registration", body);
        log.info("Sent registration approval request to {}", properties.getAdminEmail());
    }

    public void sendAccountApprovedToUser(User user) {
        String loginUrl = properties.getBaseUrl() + "/login";
        String body = """
                Hello %s,

                Your IB Farms account has been approved. You can sign in now:

                %s

                Username: %s
                """.formatted(user.getFullName(), loginUrl, user.getUsername());

        send(user.getEmail(), "IB Farms – Your account is approved", body);
    }

    private void send(String to, String subject, String body) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || !StringUtils.hasText(mailHost)) {
            log.warn("Mail is not configured (set spring.mail.host); email not sent. Subject: {}", subject);
            log.warn("Message body:\n{}", body);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(resolveFromAddress());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private String resolveFromAddress() {
        if (StringUtils.hasText(properties.getMailFrom())) {
            return properties.getMailFrom();
        }
        if (StringUtils.hasText(mailUsername)) {
            return mailUsername;
        }
        return properties.getAdminEmail();
    }
}
