package com.ibfarms.service;

import com.ibfarms.dto.RegisterDto;
import com.ibfarms.entity.User;
import com.ibfarms.exception.DuplicateResourceException;
import com.ibfarms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationApprovalService approvalService;
    private final MailService mailService;

    @Transactional
    public User register(RegisterDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new DuplicateResourceException("Passwords do not match");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        String approvalToken = approvalService.newApprovalToken();
        User user = User.builder()
                .username(dto.getUsername().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .fullName(dto.getFullName().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .enabled(false)
                .approvalToken(approvalToken)
                .build();
        user = userRepository.save(user);
        mailService.sendPendingApprovalToAdmin(user, approvalService.approvalUrl(approvalToken));
        return user;
    }
}
