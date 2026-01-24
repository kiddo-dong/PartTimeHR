package com.example.PartTimeHR.analysis.domain;

import lombok.*;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class WeeklyWorkSummary {

    private LocalDate weekStartDate;
    private LocalDate weekEndDate;

    private int totalWorkedMinutes;
    private int totalScheduledMinutes;

    private int workDays;
    private int absentDays;

    private boolean weeklyPayEligible;     // 주휴수당 대상 여부
    private int weeklyPaidMinutes;         // 주휴수당 분 (유급)
}
