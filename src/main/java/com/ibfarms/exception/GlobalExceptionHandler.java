package com.ibfarms.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/animals";
    }

    @ExceptionHandler({DuplicateResourceException.class, BusinessException.class})
    public String handleBusiness(RuntimeException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Unhandled error", ex);
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
        return "error";
    }
}
