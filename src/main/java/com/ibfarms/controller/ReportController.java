package com.ibfarms.controller;

import com.ibfarms.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public String reports(
            @RequestParam(defaultValue = "expenses") String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {
        model.addAttribute("report", reportService.build(type, from, to));
        model.addAttribute("type", type);
        model.addAttribute("from", from != null ? from : LocalDate.now().minusMonths(1));
        model.addAttribute("to", to != null ? to : LocalDate.now());
        return "reports/index";
    }
}
