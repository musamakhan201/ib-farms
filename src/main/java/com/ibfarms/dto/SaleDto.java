package com.ibfarms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SaleDto {

    @NotNull(message = "Sale date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate saleDate;

    @NotNull(message = "Sale price is required")
    @DecimalMin(value = "0.01", message = "Sale price must be greater than zero")
    private BigDecimal salePrice;

    @Size(max = 120)
    private String buyerName;

    @Size(max = 500)
    private String notes;
}
