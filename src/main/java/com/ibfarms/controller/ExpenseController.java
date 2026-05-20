package com.ibfarms.controller;

import com.ibfarms.dto.ExpenseDto;
import com.ibfarms.service.AnimalService;
import com.ibfarms.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/animals/{animalId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final AnimalService animalService;
    private final ExpenseService expenseService;

    @GetMapping("/new")
    public String form(@PathVariable Long animalId, Model model) {
        model.addAttribute("animal", animalService.getOwnedAnimal(animalId));
        model.addAttribute("expense", new ExpenseDto());
        return "expenses/form";
    }

    @PostMapping
    public String save(
            @PathVariable Long animalId,
            @Valid @ModelAttribute("expense") ExpenseDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("animal", animalService.getOwnedAnimal(animalId));
            return "expenses/form";
        }
        try {
            expenseService.add(animalId, dto);
            redirect.addFlashAttribute("successMessage", "Expense recorded.");
            return "redirect:/animals/" + animalId;
        } catch (RuntimeException ex) {
            model.addAttribute("animal", animalService.getOwnedAnimal(animalId));
            model.addAttribute("errorMessage", ex.getMessage());
            return "expenses/form";
        }
    }
}
