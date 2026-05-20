package com.ibfarms.dto;

import com.ibfarms.entity.AnimalStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AnimalDetailDto {

    private Long id;
    private String name;
    private String species;
    private String breed;
    private String tagNumber;
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
    private String pictureFilename;
    private boolean pregnant;
    private LocalDate pregnancyDate;
    private LocalDate expectedDeliveryDate;
    private AnimalStatus status;
    private BigDecimal totalExpenses;
    private BigDecimal profit;
    private SaleSummaryDto sale;
    private List<GrowthSummaryDto> growthRecords;
    private List<ExpenseSummaryDto> expenses;

    @Data
    @Builder
    public static class SaleSummaryDto {
        private LocalDate saleDate;
        private BigDecimal salePrice;
        private String buyerName;
        private String notes;
        private BigDecimal profit;
    }

    @Data
    @Builder
    public static class GrowthSummaryDto {
        private Long id;
        private LocalDate recordDate;
        private BigDecimal heightCm;
        private BigDecimal lengthCm;
        private BigDecimal weightKg;
        private String notes;
    }

    @Data
    @Builder
    public static class ExpenseSummaryDto {
        private Long id;
        private LocalDate expenseDate;
        private String category;
        private BigDecimal amount;
        private String description;
    }
}
