package com.ibfarms.util;

import java.math.BigDecimal;

public final class ProfitCalculator {

    private ProfitCalculator() {
    }

    /**
     * profit = salePrice - purchasePrice - totalExpenses
     */
    public static BigDecimal calculate(
            BigDecimal salePrice,
            BigDecimal purchasePrice,
            BigDecimal totalExpenses) {
        BigDecimal sale = salePrice != null ? salePrice : BigDecimal.ZERO;
        BigDecimal purchase = purchasePrice != null ? purchasePrice : BigDecimal.ZERO;
        BigDecimal expenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
        return sale.subtract(purchase).subtract(expenses);
    }
}
