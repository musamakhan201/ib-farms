package com.ibfarms.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeFormDto {

    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @Size(max = 80)
    private String role;

    @Size(max = 30)
    private String phone;

    @DecimalMin(value = "0.0", message = "Salary must be non-negative")
    private BigDecimal monthlySalary;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hireDate;

    private boolean active = true;
}
