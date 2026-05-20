package com.ibfarms.controller;

import com.ibfarms.dto.OtherExpenseDto;
import com.ibfarms.service.OtherExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/other-expenses")
@RequiredArgsConstructor
public class OtherExpenseController {

    private final OtherExpenseService otherExpenseService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("expenses", otherExpenseService.findAll());
        return "other-expenses/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("expense", new OtherExpenseDto());
        return "other-expenses/form";
    }

    @PostMapping
    public String save(
            @Valid @ModelAttribute("expense") OtherExpenseDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "other-expenses/form";
        }
        otherExpenseService.create(dto);
        redirect.addFlashAttribute("successMessage", "Expense recorded.");
        return "redirect:/other-expenses";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        otherExpenseService.delete(id);
        redirect.addFlashAttribute("successMessage", "Expense deleted.");
        return "redirect:/other-expenses";
    }
}
