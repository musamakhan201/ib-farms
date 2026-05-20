package com.ibfarms.controller;

import com.ibfarms.dto.SaleDto;
import com.ibfarms.service.AnimalService;
import com.ibfarms.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/animals/{animalId}/sell")
@RequiredArgsConstructor
public class SaleController {

    private final AnimalService animalService;
    private final SaleService saleService;

    @GetMapping
    public String form(@PathVariable Long animalId, Model model) {
        var animal = animalService.getOwnedAnimal(animalId);
        model.addAttribute("animal", animal);
        model.addAttribute("sale", new SaleDto());
        return "sales/form";
    }

    @PostMapping
    public String sell(
            @PathVariable Long animalId,
            @Valid @ModelAttribute("sale") SaleDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("animal", animalService.getOwnedAnimal(animalId));
            return "sales/form";
        }
        try {
            saleService.sell(animalId, dto);
            redirect.addFlashAttribute("successMessage", "Animal sold successfully. View or download the sale report below.");
            return "redirect:/animals/" + animalId + "/sale/report";
        } catch (RuntimeException ex) {
            model.addAttribute("animal", animalService.getOwnedAnimal(animalId));
            model.addAttribute("errorMessage", ex.getMessage());
            return "sales/form";
        }
    }

    @GetMapping("/preview-profit")
    @ResponseBody
    public BigDecimal previewProfit(@PathVariable Long animalId, @RequestParam BigDecimal salePrice) {
        return saleService.previewProfit(animalId, salePrice);
    }
}
