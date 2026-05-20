package com.ibfarms.controller;

import com.ibfarms.dto.EmployeeFormDto;
import com.ibfarms.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        return "employees/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("employee", new EmployeeFormDto());
        model.addAttribute("isEdit", false);
        return "employees/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("employee") EmployeeFormDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "employees/form";
        }
        employeeService.create(dto);
        redirect.addFlashAttribute("successMessage", "Employee added.");
        return "redirect:/employees";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("employee", employeeService.getOwnedWithSalaries(id));
        return "employees/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("employee", employeeService.toFormDto(employeeService.getOwned(id)));
        model.addAttribute("isEdit", true);
        return "employees/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("employee") EmployeeFormDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "employees/form";
        }
        employeeService.update(id, dto);
        redirect.addFlashAttribute("successMessage", "Employee updated.");
        return "redirect:/employees/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        employeeService.delete(id);
        redirect.addFlashAttribute("successMessage", "Employee removed.");
        return "redirect:/employees";
    }
}
