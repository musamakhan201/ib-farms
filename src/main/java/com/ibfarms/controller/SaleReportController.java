package com.ibfarms.controller;

import com.ibfarms.service.SaleReportPdfService;
import com.ibfarms.service.SaleReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/animals/{animalId}/sale")
@RequiredArgsConstructor
public class SaleReportController {

    private final SaleReportService saleReportService;
    private final SaleReportPdfService saleReportPdfService;

    @GetMapping("/report")
    public String report(@PathVariable Long animalId, Model model) {
        model.addAttribute("report", saleReportService.build(animalId));
        return "sales/report";
    }

    @GetMapping("/report/download")
    public ResponseEntity<byte[]> download(@PathVariable Long animalId) {
        var report = saleReportService.build(animalId);
        byte[] pdf = saleReportPdfService.generate(report);
        String filename = "ib-farms-sale-" + report.getTagNumber() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
