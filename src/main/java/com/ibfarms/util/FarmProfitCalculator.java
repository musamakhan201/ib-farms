package com.ibfarms.util;

import java.math.BigDecimal;

public final class FarmProfitCalculator {

    private FarmProfitCalculator() {
    }

    /**
     * Net farm profit = animal sale profits − other expenses − salary payments (in period).
     */
    public static BigDecimal netFarmProfit(
            BigDecimal grossAnimalProfit,
            BigDecimal otherExpenses,
            BigDecimal salaryPayments) {
        BigDecimal gross = grossAnimalProfit != null ? grossAnimalProfit : BigDecimal.ZERO;
        BigDecimal other = otherExpenses != null ? otherExpenses : BigDecimal.ZERO;
        BigDecimal salaries = salaryPayments != null ? salaryPayments : BigDecimal.ZERO;
        return gross.subtract(other).subtract(salaries);
    }
}
