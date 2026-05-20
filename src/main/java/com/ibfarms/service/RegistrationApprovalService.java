package com.ibfarms.service;

import com.ibfarms.config.IbFarmsProperties;
import com.ibfarms.entity.User;
import com.ibfarms.exception.ResourceNotFoundException;
import com.ibfarms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationApprovalService {

    private final UserRepository userRepository;
    private final IbFarmsProperties properties;
    private final MailService mailService;

    public String newApprovalToken() {
        return UUID.randomUUID().toString();
    }

    public String approvalUrl(String token) {
        return properties.getBaseUrl() + "/approval/approve?token=" + token;
    }

    @Transactional
    public User approve(String token) {
        User user = userRepository.findByApprovalToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired approval link"));

        if (!user.isEnabled()) {
            user.setEnabled(true);
            user.setApprovalToken(null);
            user = userRepository.save(user);
            try {
                mailService.sendAccountApprovedToUser(user);
            } catch (RuntimeException ex) {
                // Approval succeeded even if notification email fails.
            }
        }
        return user;
    }
}
