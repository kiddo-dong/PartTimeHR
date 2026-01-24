package com.example.PartTimeHR.analysis.service;

import com.example.PartTimeHR.analysis.domain.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class WeeklyWorkSummaryService {

    public List<WeeklyWorkSummary> summarize(
            List<DailyWorkStatus> dailyStatuses,
            int weekStartDay,
            boolean weeklyPayApplicable
    ) {

        Map<LocalDate, List<DailyWorkStatus>> weeklyMap = new HashMap<>();

        for (DailyWorkStatus status : dailyStatuses) {
            LocalDate weekStartDate =
                    calculateWeekStartDate(status.getDate(), weekStartDay);

            weeklyMap
                    .computeIfAbsent(weekStartDate, k -> new ArrayList<>())
                    .add(status);
        }

        List<WeeklyWorkSummary> result = new ArrayList<>();

        for (Map.Entry<LocalDate, List<DailyWorkStatus>> entry : weeklyMap.entrySet()) {

            LocalDate weekStartDate = entry.getKey();
            LocalDate weekEndDate = weekStartDate.plusDays(6);

            int totalWorkedMinutes = 0;
            int totalScheduledMinutes = 0;
            int workDays = 0;
            int absentDays = 0;

            for (DailyWorkStatus status : entry.getValue()) {

                if (status.isScheduled()) {
                    totalScheduledMinutes += status.getScheduledMinutes();
                }

                if (status.isWorked()) {
                    totalWorkedMinutes += status.getWorkedMinutes();
                    workDays++;
                }

                if (status.getAttendanceStatus() == AttendanceStatus.ABSENT) {
                    absentDays++;
                }
            }

            // 주휴수당 판단
            boolean weeklyPayEligible =
                    weeklyPayApplicable
                            && totalScheduledMinutes >= 15 * 60
                            && absentDays == 0;

            // ✅ 주휴수당 시간 계산
            int weeklyPaidMinutes = 0;
            if (weeklyPayEligible && workDays > 0) {
                weeklyPaidMinutes = totalScheduledMinutes / workDays;
            }

            result.add(
                    WeeklyWorkSummary.builder()
                            .weekStartDate(weekStartDate)
                            .weekEndDate(weekEndDate)
                            .totalWorkedMinutes(totalWorkedMinutes)
                            .totalScheduledMinutes(totalScheduledMinutes)
                            .workDays(workDays)
                            .absentDays(absentDays)
                            .weeklyPayEligible(weeklyPayEligible)
                            .weeklyPaidMinutes(weeklyPaidMinutes)
                            .build()
            );
        }

        return result;
    }

    private LocalDate calculateWeekStartDate(
            LocalDate date,
            int weekStartDay
    ) {
        DayOfWeek startDay = DayOfWeek.of(weekStartDay);

        while (date.getDayOfWeek() != startDay) {
            date = date.minusDays(1);
        }
        return date;
    }
}
