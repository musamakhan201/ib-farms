package com.ibfarms.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ReportDto {

    private String reportType;
    private LocalDate from;
    private LocalDate to;
    private BigDecimal totalExpenses;
    private BigDecimal totalAnimalExpenses;
    private BigDecimal totalOtherExpenses;
    private BigDecimal totalSalaryPayments;
    private BigDecimal totalSales;
    private BigDecimal totalProfit;
    private BigDecimal grossAnimalProfit;
    private BigDecimal netFarmProfit;
    private List<ExpenseLineDto> expenses;
    private List<SaleLineDto> sales;
    private List<ProfitLineDto> profits;
    private List<PregnancyLineDto> pregnancies;
    private List<GrowthLineDto> growth;
    private List<CategoryTotalDto> expenseByCategory;
    private List<OtherExpenseLineDto> otherExpenses;
    private List<SalaryLineDto> salaryPayments;
    private List<String> chartLabels;
    private List<BigDecimal> chartData;

    @Data
    @Builder
    public static class ExpenseLineDto {
        private String expenseType;
        private LocalDate date;
        private String animalName;
        private String tagNumber;
        private String category;
        private BigDecimal amount;
        private String description;
    }

    @Data
    @Builder
    public static class OtherExpenseLineDto {
        private LocalDate date;
        private String category;
        private BigDecimal amount;
        private String description;
    }

    @Data
    @Builder
    public static class SalaryLineDto {
        private LocalDate paidDate;
        private String employeeName;
        private String role;
        private String monthLabel;
        private BigDecimal amount;
        private String notes;
    }

    @Data
    @Builder
    public static class SaleLineDto {
        private LocalDate saleDate;
        private String animalName;
        private String tagNumber;
        private BigDecimal salePrice;
        private BigDecimal purchasePrice;
        private BigDecimal expenses;
        private BigDecimal profit;
        private String buyerName;
    }

    @Data
    @Builder
    public static class ProfitLineDto {
        private String animalName;
        private String tagNumber;
        private BigDecimal salePrice;
        private BigDecimal purchasePrice;
        private BigDecimal expenses;
        private BigDecimal profit;
    }

    @Data
    @Builder
    public static class PregnancyLineDto {
        private String name;
        private String tagNumber;
        private String species;
        private LocalDate pregnancyDate;
        private LocalDate expectedDeliveryDate;
        private long daysRemaining;
    }

    @Data
    @Builder
    public static class GrowthLineDto {
        private String animalName;
        private String tagNumber;
        private LocalDate recordDate;
        private BigDecimal heightCm;
        private BigDecimal lengthCm;
        private BigDecimal weightKg;
    }

    @Data
    @Builder
    public static class CategoryTotalDto {
        private String category;
        private BigDecimal total;
    }
}
