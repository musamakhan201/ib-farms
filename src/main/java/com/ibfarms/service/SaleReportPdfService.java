package com.ibfarms.service;

import com.ibfarms.dto.SaleReportDto;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class SaleReportPdfService {

    private final SpringTemplateEngine templateEngine;

    public byte[] generate(SaleReportDto report) {
        Context context = new Context();
        context.setVariable("report", report);
        context.setVariable("pdfMode", true);
        String html = templateEngine.process("sales/report-document", context);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate PDF report", ex);
        }
    }
}
