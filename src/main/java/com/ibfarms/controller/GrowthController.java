package com.ibfarms.controller;

import com.ibfarms.dto.GrowthRecordDto;
import com.ibfarms.service.AnimalService;
import com.ibfarms.service.GrowthRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/animals/{animalId}/growth")
@RequiredArgsConstructor
public class GrowthController {

    private final AnimalService animalService;
    private final GrowthRecordService growthRecordService;

    @GetMapping("/new")
    public String form(@PathVariable Long animalId, Model model) {
        model.addAttribute("animal", animalService.getOwnedAnimal(animalId));
        model.addAttribute("growth", new GrowthRecordDto());
        return "growth/form";
    }

    @PostMapping
    public String save(
            @PathVariable Long animalId,
            @Valid @ModelAttribute("growth") GrowthRecordDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("animal", animalService.getOwnedAnimal(animalId));
            return "growth/form";
        }
        try {
            growthRecordService.add(animalId, dto);
            redirect.addFlashAttribute("successMessage", "Growth record saved.");
            return "redirect:/animals/" + animalId;
        } catch (RuntimeException ex) {
            model.addAttribute("animal", animalService.getOwnedAnimal(animalId));
            model.addAttribute("errorMessage", ex.getMessage());
            return "growth/form";
        }
    }
}
