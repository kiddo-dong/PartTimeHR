package com.example.PartTimeHR.analysis.domain;

import lombok.*;

import java.time.YearMonth;

@Getter
@AllArgsConstructor
@Builder
public class MonthlyWorkSummary {

    private YearMonth yearMonth;

    private int totalWorkedMinutes;
    private int totalScheduledMinutes;

    private int totalOvertimeMinutes;
    private int totalWeeklyPaidMinutes;

    private int workDays;
    private int absentDays;

    private int weeklyPayEligibleWeeks;
}
