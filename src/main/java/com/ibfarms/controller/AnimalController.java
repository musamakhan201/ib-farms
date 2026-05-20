package com.ibfarms.controller;

import com.ibfarms.dto.AnimalFormDto;
import com.ibfarms.dto.GrowthRecordDto;
import com.ibfarms.entity.AnimalStatus;
import com.ibfarms.service.AnimalService;
import com.ibfarms.service.GrowthRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/animals")
@RequiredArgsConstructor
public class AnimalController {

    private final AnimalService animalService;
    private final GrowthRecordService growthRecordService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) AnimalStatus status,
            @RequestParam(required = false) Boolean pregnant,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var animals = animalService.search(search, species, status, pregnant, pageable);

        model.addAttribute("animals", animals);
        model.addAttribute("search", search);
        model.addAttribute("species", species);
        model.addAttribute("status", status);
        model.addAttribute("pregnant", pregnant);
        model.addAttribute("speciesOptions", animalService.speciesOptions());
        model.addAttribute("statuses", AnimalStatus.values());
        return "animals/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("animal", new AnimalFormDto());
        model.addAttribute("isEdit", false);
        return "animals/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("animal") AnimalFormDto dto,
            BindingResult result,
            @RequestParam(value = "picture", required = false) MultipartFile picture,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "animals/form";
        }
        try {
            var saved = animalService.create(dto, picture);
            if (dto.isRecordInitialGrowth()) {
                GrowthRecordDto growth = new GrowthRecordDto();
                growth.setRecordDate(dto.getGrowthRecordDate() != null ? dto.getGrowthRecordDate() : dto.getPurchaseDate());
                growth.setHeightCm(dto.getGrowthHeightCm());
                growth.setLengthCm(dto.getGrowthLengthCm());
                growth.setWeightKg(dto.getGrowthWeightKg());
                growth.setNotes(dto.getGrowthNotes());
                growthRecordService.add(saved.getId(), growth);
            }
            redirect.addFlashAttribute("successMessage", "Animal added successfully.");
            return "redirect:/animals";
        } catch (Exception ex) {
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", ex.getMessage());
            return "animals/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("animal", animalService.getDetail(id));
        return "animals/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("animal", animalService.toFormDto(animalService.getOwnedAnimal(id)));
        model.addAttribute("isEdit", true);
        return "animals/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("animal") AnimalFormDto dto,
            BindingResult result,
            @RequestParam(value = "picture", required = false) MultipartFile picture,
            Model model,
            RedirectAttributes redirect) {
        dto.setId(id);
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "animals/form";
        }
        try {
            animalService.update(id, dto, picture);
            redirect.addFlashAttribute("successMessage", "Animal updated successfully.");
            return "redirect:/animals/" + id;
        } catch (Exception ex) {
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", ex.getMessage());
            return "animals/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            animalService.delete(id);
            redirect.addFlashAttribute("successMessage", "Animal deleted.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/animals";
    }
}
