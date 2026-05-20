package com.ibfarms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GrowthRecordDto {

    @NotNull(message = "Record date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate recordDate;

    @DecimalMin(value = "0.0", message = "Height must be non-negative")
    private BigDecimal heightCm;

    @DecimalMin(value = "0.0", message = "Length must be non-negative")
    private BigDecimal lengthCm;

    @DecimalMin(value = "0.0", message = "Weight must be non-negative")
    private BigDecimal weightKg;

    private String notes;
}
