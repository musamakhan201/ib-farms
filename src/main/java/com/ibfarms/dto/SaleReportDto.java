package com.ibfarms.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SaleReportDto {

    private Long animalId;
    private String animalName;
    private String tagNumber;
    private String species;
    private String breed;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private LocalDate saleDate;
    private BigDecimal salePrice;
    private String buyerName;
    private String saleNotes;
    private BigDecimal totalExpenses;
    private BigDecimal profit;
    private boolean profitLoss;
    private List<ExpenseLine> expenses;
    private String generatedAt;

    @Data
    @Builder
    public static class ExpenseLine {
        private LocalDate date;
        private String category;
        private BigDecimal amount;
        private String description;
    }
}
