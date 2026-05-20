package com.ibfarms.controller;

import com.ibfarms.dto.SalaryPaymentDto;
import com.ibfarms.service.EmployeeService;
import com.ibfarms.service.SalaryPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/employees/{employeeId}/salaries")
@RequiredArgsConstructor
public class SalaryPaymentController {

    private final EmployeeService employeeService;
    private final SalaryPaymentService salaryPaymentService;

    @GetMapping("/new")
    public String form(@PathVariable Long employeeId, Model model) {
        var employee = employeeService.getOwned(employeeId);
        model.addAttribute("employee", employee);
        SalaryPaymentDto dto = new SalaryPaymentDto();
        LocalDate today = LocalDate.now();
        dto.setSalaryYear(today.getYear());
        dto.setSalaryMonth(today.getMonthValue());
        dto.setPaidDate(today);
        if (employee.getMonthlySalary() != null) {
            dto.setAmount(employee.getMonthlySalary());
        }
        model.addAttribute("salary", dto);
        return "employees/salary-form";
    }

    @PostMapping
    public String save(
            @PathVariable Long employeeId,
            @Valid @ModelAttribute("salary") SalaryPaymentDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("employee", employeeService.getOwned(employeeId));
            return "employees/salary-form";
        }
        try {
            salaryPaymentService.record(employeeId, dto);
            redirect.addFlashAttribute("successMessage", "Salary payment recorded.");
            return "redirect:/employees/" + employeeId;
        } catch (RuntimeException ex) {
            model.addAttribute("employee", employeeService.getOwned(employeeId));
            model.addAttribute("errorMessage", ex.getMessage());
            return "employees/salary-form";
        }
    }

    @PostMapping("/{paymentId}/delete")
    public String delete(
            @PathVariable Long employeeId,
            @PathVariable Long paymentId,
            RedirectAttributes redirect) {
        salaryPaymentService.delete(employeeId, paymentId);
        redirect.addFlashAttribute("successMessage", "Salary payment deleted.");
        return "redirect:/employees/" + employeeId;
    }
}
