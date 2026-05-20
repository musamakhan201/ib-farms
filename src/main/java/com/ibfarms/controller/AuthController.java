package com.ibfarms.controller;

import com.ibfarms.dto.RegisterDto;
import com.ibfarms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerDto") RegisterDto dto,
            BindingResult result,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.register(dto);
            redirect.addFlashAttribute("successMessage",
                    "Registration received. Your account is pending admin approval — you can sign in after approval.");
            return "redirect:/login";
        } catch (RuntimeException ex) {
            result.reject("registration", ex.getMessage());
            return "register";
        }
    }
}
