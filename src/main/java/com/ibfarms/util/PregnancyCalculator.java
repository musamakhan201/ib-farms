package com.ibfarms.util;

import java.time.LocalDate;

public final class PregnancyCalculator {

    public static final int GESTATION_DAYS = 283;

    private PregnancyCalculator() {
    }

    public static LocalDate expectedDeliveryDate(LocalDate pregnancyDate) {
        if (pregnancyDate == null) {
            return null;
        }
        return pregnancyDate.plusDays(GESTATION_DAYS);
    }
}
