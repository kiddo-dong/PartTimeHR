package com.example.PartTimeHR.analysis.service;

import com.example.PartTimeHR.analysis.domain.*;

import java.time.YearMonth;
import java.util.*;

public class MonthlyWorkSummaryService {

    public MonthlyWorkSummary summarize(
            YearMonth yearMonth,
            List<WeeklyWorkSummary> weeklySummaries
    ) {

        int totalWorkedMinutes = 0;
        int totalScheduledMinutes = 0;
        int totalOvertimeMinutes = 0;
        int totalWeeklyPaidMinutes = 0;
        int workDays = 0;
        int absentDays = 0;
        int weeklyPayEligibleWeeks = 0;

        for (WeeklyWorkSummary weekly : weeklySummaries) {

            // ✅ 해당 월에 속한 주만 집계
            if (!isWeekInMonth(weekly, yearMonth)) {
                continue;
            }

            totalWorkedMinutes += weekly.getTotalWorkedMinutes();
            totalScheduledMinutes += weekly.getTotalScheduledMinutes();
            totalWeeklyPaidMinutes += weekly.getWeeklyPaidMinutes();

            workDays += weekly.getWorkDays();
            absentDays += weekly.getAbsentDays();

            if (weekly.isWeeklyPayEligible()) {
                weeklyPayEligibleWeeks++;
            }

            // 연장근무 분은 지금 구조상 weekly에 없으면 0
            // (나중에 weekly에 overtimeMinutes 넣으면 여기서 합산)
        }

        return MonthlyWorkSummary.builder()
                .yearMonth(yearMonth)
                .totalWorkedMinutes(totalWorkedMinutes)
                .totalScheduledMinutes(totalScheduledMinutes)
                .totalOvertimeMinutes(totalOvertimeMinutes)
                .totalWeeklyPaidMinutes(totalWeeklyPaidMinutes)
                .workDays(workDays)
                .absentDays(absentDays)
                .weeklyPayEligibleWeeks(weeklyPayEligibleWeeks)
                .build();
    }

    // === 주가 해당 월과 겹치는지 판단 ===
    private boolean isWeekInMonth(
            WeeklyWorkSummary weekly,
            YearMonth yearMonth
    ) {
        return !weekly.getWeekEndDate().isBefore(yearMonth.atDay(1))
                && !weekly.getWeekStartDate().isAfter(yearMonth.atEndOfMonth());
    }
}
