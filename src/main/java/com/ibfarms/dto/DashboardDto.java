package com.ibfarms.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardDto {

    private long totalAnimals;
    private long activeAnimals;
    private long soldAnimals;
    private long pregnantAnimals;
    private BigDecimal monthlyExpenses;
    private BigDecimal monthlySales;
    private BigDecimal monthlyProfit;
    private List<String> expenseChartLabels;
    private List<BigDecimal> expenseChartData;
    private List<String> salesChartLabels;
    private List<BigDecimal> salesChartData;
    private List<PregnancyAlertDto> upcomingDeliveries;

    @Data
    @Builder
    public static class PregnancyAlertDto {
        private Long animalId;
        private String name;
        private String tagNumber;
        private String expectedDeliveryDate;
        private long daysRemaining;
    }
}
