package com.ibfarms.controller;

import com.ibfarms.entity.User;
import com.ibfarms.exception.ResourceNotFoundException;
import com.ibfarms.service.RegistrationApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final RegistrationApprovalService approvalService;

    @GetMapping("/approve")
    public String approve(@RequestParam(required = false) String token, Model model) {
        if (!StringUtils.hasText(token)) {
            model.addAttribute("message", "Missing approval link.");
            return "approval/error";
        }
        try {
            User user = approvalService.approve(token.trim());
            model.addAttribute("username", user.getUsername());
            return "approval/success";
        } catch (ResourceNotFoundException ex) {
            model.addAttribute("message", ex.getMessage());
            return "approval/error";
        }
    }
}
