package com.example.PartTimeHR.schedule.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class ScheduleDateCalculator {

    public static LocalDate getWeekStartDate(
            LocalDate baseDate,
            int weekStartDay
    ) {
        DayOfWeek startDay = DayOfWeek.of(weekStartDay);

        int diff = baseDate.getDayOfWeek().getValue() - startDay.getValue();

        if (diff < 0) {
            diff += 7;
        }

        return baseDate.minusDays(diff);
    }
}
