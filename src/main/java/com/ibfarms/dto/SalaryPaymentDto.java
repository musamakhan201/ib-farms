package com.ibfarms.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalaryPaymentDto {

    @NotNull(message = "Salary year is required")
    @Min(2000)
    @Max(2100)
    private Integer salaryYear;

    @NotNull(message = "Salary month is required")
    @Min(1)
    @Max(12)
    private Integer salaryMonth;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Paid date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate paidDate;

    @Size(max = 500)
    private String notes;
}
