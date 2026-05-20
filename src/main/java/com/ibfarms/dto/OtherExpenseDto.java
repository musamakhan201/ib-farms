package com.ibfarms.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OtherExpenseDto {

    @NotNull(message = "Expense date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expenseDate;

    @NotBlank(message = "Category is required")
    @Size(max = 60)
    private String category;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Size(max = 500)
    private String description;
}
